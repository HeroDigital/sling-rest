package com.herodigital.wcm.internal.rest.service.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herodigital.wcm.internal.rest.registry.RestFunctionRegistry;
import com.herodigital.wcm.internal.rest.registry.RestFunctionRegistry.ResolvedFunction;
import com.herodigital.wcm.internal.rest.registry.RestFunctionRegistryTrie;
import com.herodigital.wcm.internal.rest.registry.RestOperation;
import com.herodigital.wcm.internal.rest.service.SlingRestFunction;
import com.herodigital.wcm.internal.rest.service.SlingRestService;
import com.herodigital.wcm.internal.rest.service.RestFunctionRegistryService;


@Component(immediate = true, metatype = false, label="Sling REST Registry Service", description="Maintains registry of all registered SlingRestService services")
@Service(RestFunctionRegistryService.class)
@Properties({
	@Property(name = "service.vendor", value = "Hero Digital"),
	@Property(name = "service.description", value = "Maintains registry of all registered SlingRestService services"),
})
public class RestFunctionRegistryServiceImpl implements RestFunctionRegistryService {
	
	private static final Logger log = LoggerFactory.getLogger(RestFunctionRegistryServiceImpl.class);
	
	private ServiceTracker tracker;
	
	private volatile int _trackingCount;
	
	private RestFunctionRegistry<FunctionMeta> registry;
	
	@Activate
	public void activate(ComponentContext context) throws InvalidSyntaxException {
		BundleContext bc = context.getBundleContext();
		Filter filter = bc.createFilter("("+Constants.OBJECTCLASS + "=" + SlingRestService.class.getName()+")");
		tracker = new ServiceTracker(bc, filter, null);
		tracker.open();
		
		registry = new RestFunctionRegistryTrie<>();
		
		_trackingCount = -1;
		rebuildRegistryIfNeeded();
	}
	
	@Deactivate
	public void deactivate() {
		tracker.close();
	}

	@Override
	public ResolvedFunction<FunctionMeta> getFunction(RestOperation restOperation) {
		rebuildRegistryIfNeeded();
		return registry.getFunction(restOperation);
	}
	
	protected void rebuildRegistryIfNeeded() {
		int actualCount = tracker.getTrackingCount();
		
		// Cache the value of the volatile _trackingCount to minimize overhead
		// incurred by accessing live value multiple times needlessly. (in theory)
		int localTrackingCount = _trackingCount;

		// double checked locking with volatile
		if (localTrackingCount != actualCount) {
			synchronized(this) {
				localTrackingCount = _trackingCount;
				if (localTrackingCount != actualCount) {
					
					log.info("Rebuilding REST Function Registry. Tracking count mismatch: cached = {}, actual = {}", localTrackingCount, actualCount);
					
					registry.clear();
					
					ServiceReference[] srs = tracker.getServiceReferences();
					localTrackingCount = tracker.getTrackingCount();
					if (srs == null) {
						log.warn("No {} services have been registered", SlingRestService.class);
						return;
					}
					
					log.info("Found {} service matches", srs.length);
					
					for (ServiceReference sr : srs) {
						SlingRestService heroWebService = (SlingRestService) tracker.getService(sr);
						List<FunctionMeta> funcMetas = getFunctionMeta(heroWebService);
						for (FunctionMeta funcMeta : funcMetas) {
							log.info("Registering {} to {}.{}", new Object[]{funcMeta.getOperation(), heroWebService.getClass().getName(), funcMeta.getJavaMethod()});
							registry.addFunction(funcMeta.getOperation(), funcMeta);
						}
					}
					
					// finally update shared _trackingCount
					_trackingCount = localTrackingCount;
				} else {
					log.trace("trackingCounts match after double check, no refresh. {}={}", localTrackingCount, actualCount);
				}
			}
		} else {
			log.trace("trackingCounts match, no refresh. {}={}", localTrackingCount, actualCount);
		}
	}
	
	private List<FunctionMeta> getFunctionMeta(final SlingRestService heroWebService) {
	    final List<FunctionMeta> ops = new ArrayList<>();
	    Class<?> clazz = heroWebService.getClass();
	    if (clazz != Object.class) {
	        final Method[] allMethods = clazz.getDeclaredMethods();       
	        for (final Method method : allMethods) {
	            if (method.isAnnotationPresent(SlingRestFunction.class)) {
	            	SlingRestFunction annotation = method.getAnnotation(SlingRestFunction.class);
	            	String[] opsStr = annotation.value();
	            	for (String opStr : opsStr) {
		            	RestOperation op = RestOperation.fromString(opStr);
		            	ops.add(new FunctionMeta(heroWebService, method.getName(), op));
	            	}
	            }
	        }
	        clazz = clazz.getSuperclass();
	    }
	    return ops;
	}

}

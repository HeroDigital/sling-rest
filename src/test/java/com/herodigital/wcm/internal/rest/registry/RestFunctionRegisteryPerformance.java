package com.herodigital.wcm.internal.rest.registry;

import java.util.LinkedList;
import java.util.List;

import com.herodigital.wcm.internal.rest.registry.RestFunctionRegistry.ResolvedFunction;

public class RestFunctionRegisteryPerformance {
	
	public static void main(String[] args) {
		RestFunctionRegistry<String> trie = new RestFunctionRegistryTrie<>();
		RestFunctionRegistry<String> list = new RestRunctionRegistryList<>();
		
		System.out.println("Generating operation set...");
		List<RestOperation> ops = generateLoadSet();
//		for (RestOperation o : ops) {
//			System.out.println(o);
//		}
		System.out.println("done " + ops.size());
		
		System.out.println("------------------------------");
		
		load("trie", trie, ops);
		trie.clear();
		load("trie", trie, ops);
		
		//System.out.println(trie);

		System.out.println("++++++++++++");
		load("list", list, ops);
		list.clear();
		load("list", list, ops);
		
		System.out.println("------------------------------");
		
		List<RestOperation> opsPull = generatePullSet(ops);
		
		pull("list", list, opsPull);
		pull("list", list, opsPull);
		
		System.out.println("++++++++++++");
		
		pull("trie", trie, opsPull);
		pull("trie", trie, opsPull);
		
		System.out.println("------------------------------");
		
		pullNulls("list", list, opsPull.size());
		pullNulls("list", list, opsPull.size());
		
		System.out.println("++++++++++++");

		pullNulls("trie", trie, opsPull.size());
		pullNulls("trie", trie, opsPull.size());
	}

	public static List<RestOperation> generateLoadSet() {
		String[] bases = new String[]{"/api", "/rpc"};
		String[] subA = new String[]{"user", "place", "color", "category"};
		String[] subB = new String[]{"day", "month", "year", "hour", "minute", "second", "milli"};
		String[] subC = new String[]{"honda", "audi", "gm", "ford", "pontiac", "toyota", "kia", "saturn", "jaguar", "porsche", "fiat", "mazda", "chrysler", "subaru"};
		
		List<RestOperation> ops = new LinkedList<RestOperation>();
		for (String base : bases) {
			String start = base + "/{wildcard}";
			for (String a : subA) {
				String startA = start;
				startA += "/" + a;
				
				for (String b : subB) {
					String startAB = startA;
					startAB += "/" + b;
					
					for (String c : subC) {
						String startABC = startAB;
						startABC += "/" + c;
						
						ops.add(new RestOperation(HttpMethod.GET, startABC));
						ops.add(new RestOperation(HttpMethod.GET, startABC + "/{id}"));
						ops.add(new RestOperation(HttpMethod.GET, startABC.replace("/{wildcard}", "")));
						ops.add(new RestOperation(HttpMethod.POST, startABC));
						ops.add(new RestOperation(HttpMethod.POST, startABC + "/{id}"));
						ops.add(new RestOperation(HttpMethod.POST, startABC.replace("/{wildcard}", "")));
						ops.add(new RestOperation(HttpMethod.PUT, startABC));
						ops.add(new RestOperation(HttpMethod.PUT, startABC + "/{id}"));
						ops.add(new RestOperation(HttpMethod.PUT, startABC.replace("/{wildcard}", "")));
						ops.add(new RestOperation(HttpMethod.DELETE, startABC));
						ops.add(new RestOperation(HttpMethod.DELETE, startABC + "/{id}"));
						ops.add(new RestOperation(HttpMethod.DELETE, startABC.replace("/{wildcard}", "")));
					}
				}
			}
		}
		
		return ops;
	}
	
	
	public static List<RestOperation> generatePullSet(List<RestOperation> ops) {
		List<RestOperation> result = new LinkedList<RestOperation>();
		for (RestOperation o : ops) {
			String path = o.getPath();
			path = path.replace("{wildcard}", "VALUE1");
			path = path.replace("{id}", "123");
			RestOperation pull = new RestOperation(o.getMethod(), path);
			result.add(pull);
		}
		return result;
	}

	public static void load(String name, RestFunctionRegistry<String> or, List<RestOperation> ops) {
		System.out.println("Loading " + name);
		long start = now();
		for (RestOperation o : ops) {
			or.addFunction(o, o.getPath());
		}
		long stop = now(); 
		System.out.println("done " + diff(start, stop));
	}
	
	public static void pull(String name, RestFunctionRegistry<String> or, List<RestOperation> ops) {
		System.out.println("Pulling " + name);
		
		long start = now();
		for (RestOperation o : ops) {
			ResolvedFunction<String> s = or.getFunction(o);
			assert s != null;
			//System.out.println(s + " = " + o.getPath());
		}
		long stop = now(); 
		
		System.out.println("done " + diff(start, stop));
	}
	
	public static void pullNulls(String name, RestFunctionRegistry<String> or, int size) {
		System.out.println("Pulling nulls " + name);
		
		long start = now();
		for (int i = 0; i < size; i++) {
			ResolvedFunction<String> s = or.getFunction(new RestOperation(HttpMethod.GET, "/rpc/something/category/milli/pontiac_notfound/id"));
			assert s == null;
			
		}
		long stop = now(); 
		
		System.out.println("done " + diff(start, stop));
	}
	
	public static long now() {
		return System.currentTimeMillis();
	}
	
	public static long diff(long start, long stop) {
		return stop - start;
	}
	
}

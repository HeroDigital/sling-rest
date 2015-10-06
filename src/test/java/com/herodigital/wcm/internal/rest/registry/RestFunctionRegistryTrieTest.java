package com.herodigital.wcm.internal.rest.registry;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.herodigital.wcm.internal.rest.registry.RestFunctionRegistry.ResolvedFunction;

public class RestFunctionRegistryTrieTest {
	
	public RestFunctionRegistry<String> sut;
	
	@Before
	public void before() {
		this.sut = new RestFunctionRegistryTrie<>();
		//this.sut = new OperationRegistryLinear<>();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddException1() {
		RestOperation op1 = new RestOperation(HttpMethod.GET, "/api/basic/op1");
		
		sut.addFunction(op1, "op1");
		sut.addFunction(op1, "dup");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddException2() {
		RestOperation op1 = new RestOperation(HttpMethod.GET, "/api/basic/{something}");
		RestOperation op2 = new RestOperation(HttpMethod.GET, "/api/basic/{something2}");
		
		sut.addFunction(op1, "op1");
		sut.addFunction(op2, "op2");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddException3() {
		RestOperation op1 = new RestOperation(HttpMethod.GET, "/api/{type}/op1");
		RestOperation op2 = new RestOperation(HttpMethod.GET, "/api/{call}/op2");
		
		sut.addFunction(op1, "op1");
		sut.addFunction(op2, "op2");
	}
	
	@Test
	public void testGetAddNull() {
		RestOperation opReg = new RestOperation(HttpMethod.GET, "/api/basic/op2/register");
		RestOperation opFind = new RestOperation(HttpMethod.GET, "/api/basic/op2");
		sut.addFunction(opReg, "opReg");
		System.out.println(sut.toString());
		assertEquals(null, sut.getFunction(opFind));
	}
	
	@Test
	public void testGetAddMethod() {
		RestOperation opGet 	= new RestOperation(HttpMethod.GET, 	"/api/basic/op2/register");
		RestOperation opPost 	= new RestOperation(HttpMethod.POST, 	"/api/basic/op2/register");
		RestOperation opDelete 	= new RestOperation(HttpMethod.DELETE, 	"/api/basic/op2/register");
		RestOperation opPut 	= new RestOperation(HttpMethod.PUT, 	"/api/basic/op2/register");
		
		sut.addFunction(opGet, 		"get");
		sut.addFunction(opPost, 	"post");
		sut.addFunction(opDelete, 	"delete");
		sut.addFunction(opPut, 		"put");
		
		System.out.println(sut.toString());
		
		assertEquals("get", 	sut.getFunction(opGet).getFunction());
		assertEquals("post", 	sut.getFunction(opPost).getFunction());
		assertEquals("delete", 	sut.getFunction(opDelete).getFunction());
		assertEquals("put", 	sut.getFunction(opPut).getFunction());
	}

	@Test
	public void testGetAddBasic() {
		RestOperation op1 = new RestOperation(HttpMethod.GET, "/api/basic/op1");
		RestOperation op2 = new RestOperation(HttpMethod.GET, "/api/basic/op2");
		RestOperation op3 = new RestOperation(HttpMethod.GET, "/api/basic/sub/op3");
		RestOperation op4 = new RestOperation(HttpMethod.GET, "/api/op4");
		RestOperation op5 = new RestOperation(HttpMethod.GET, "/api");
		
		sut.addFunction(op1, "op1");
		sut.addFunction(op2, "op2");
		sut.addFunction(op3, "op3");
		sut.addFunction(op4, "op4");
		sut.addFunction(op5, "op5");
		
		System.out.println(sut.toString());
		
		assertEquals("op1", sut.getFunction(op1).getFunction());
		assertEquals("op2", sut.getFunction(op2).getFunction());
		assertEquals("op3", sut.getFunction(op3).getFunction());
		assertEquals("op4", sut.getFunction(op4).getFunction());
		assertEquals("op5", sut.getFunction(op5).getFunction());
	}
	
	@Test
	public void testGetAddWildcardSingle() {
		RestOperation op1 = new RestOperation(HttpMethod.GET, "/api/basic/op1");
		
		RestOperation op2Add = new RestOperation(HttpMethod.GET, "/api/basic/op2/{id}");
		RestOperation op2Get = new RestOperation(HttpMethod.GET, "/api/basic/op2/123");
		
		sut.addFunction(op1, "op1");
		sut.addFunction(op2Add, "op2");
		
		System.out.println(sut.toString());
		
		assertEquals("op1", sut.getFunction(op1).getFunction());
		
		ResolvedFunction<String> op2GetResult = sut.getFunction(op2Get);
		assertEquals("op2", op2GetResult.getFunction());
		assertEquals(1, 	op2GetResult.getWildcards().size());
		assertEquals("123", op2GetResult.getWildcards().get("id"));
		
		RestOperation op2Add123 = new RestOperation(HttpMethod.GET, "/api/basic/op2/123");
		RestOperation op2Get456 = new RestOperation(HttpMethod.GET, "/api/basic/op2/456");
		
		sut.addFunction(op2Add123, "op123");
		
		System.out.println(sut.toString());
		
		assertEquals("op123", sut.getFunction(op2Add123).getFunction());
		
		ResolvedFunction<String> op2Get456Result = sut.getFunction(op2Get456);
		assertEquals("op2", op2Get456Result.getFunction());
		assertEquals(1, 	op2Get456Result.getWildcards().size());
		assertEquals("456", op2Get456Result.getWildcards().get("id"));
		
	}
	
	@Test
	public void testGetAddWildcardMulti() {
		RestOperation op1 = new RestOperation(HttpMethod.GET, "/api/basic/op1");
		
		RestOperation op2Add 		= new RestOperation(HttpMethod.GET, "/api/{fork}/op2/{id}");
		RestOperation op2GetBasic 	= new RestOperation(HttpMethod.GET, "/api/basic/op2/123");
		RestOperation op2GetOther 	= new RestOperation(HttpMethod.GET, "/api/other/op2/456");
		
		sut.addFunction(op1, "op1");
		sut.addFunction(op2Add, "op2");
		
		System.out.println(sut.toString());
		
		assertEquals("op1", sut.getFunction(op1).getFunction());
		assertEquals(0, sut.getFunction(op1).getWildcards().size());
		
		ResolvedFunction<String> op2GetBasicResult = sut.getFunction(op2GetBasic);
		assertEquals("op2", 	op2GetBasicResult.getFunction());
		assertEquals(2, 		op2GetBasicResult.getWildcards().size());
		assertEquals("basic", 	op2GetBasicResult.getWildcards().get("fork"));
		assertEquals("123", 	op2GetBasicResult.getWildcards().get("id"));
		
		ResolvedFunction<String> op2GetOtherResult = sut.getFunction(op2GetOther);
		assertEquals("op2", 	op2GetOtherResult.getFunction());
		assertEquals(2, 		op2GetOtherResult.getWildcards().size());
		assertEquals("other", 	op2GetOtherResult.getWildcards().get("fork"));
		assertEquals("456", 	op2GetOtherResult.getWildcards().get("id"));
	}
	
	@Test
	public void testGetAddWildCardOverlap() {
		RestOperation op1 		= new RestOperation(HttpMethod.GET, "/api/{fork}/hello/{id}");
		RestOperation op1Get 	= new RestOperation(HttpMethod.GET, "/api/basic/hello/123");
		RestOperation op2 		= new RestOperation(HttpMethod.GET, "/api/{fork}");
		RestOperation op2Get 	= new RestOperation(HttpMethod.GET, "/api/basic");
		
		sut.addFunction(op1, "op1");
		sut.addFunction(op2, "op2");
		
		System.out.println(sut.toString());
		
		assertEquals("op1", sut.getFunction(op1Get).getFunction());
		assertEquals("op2", sut.getFunction(op2Get).getFunction());
	}
	
	@Test
	public void testGetAddWildCardOverlap1() {
		RestOperation op1Get 	= new RestOperation(HttpMethod.GET, "/api/monster/hellloo/bunnies");
		RestOperation op1 		= new RestOperation(HttpMethod.GET, "/api/{wild1}/{wild2}/{wild3}");
		RestOperation op2	 	= new RestOperation(HttpMethod.GET, "/api/{wild1}/hellloo/{wild2}/something");
		
		sut.addFunction(op1, "op1");
		sut.addFunction(op2, "op2");
		
		System.out.println(sut.toString());
		
		ResolvedFunction<String> op1Result = sut.getFunction(op1Get);
		assertEquals("op1", 	op1Result.getFunction());
		assertEquals(3,			op1Result.getWildcards().size());
		assertEquals("monster",	op1Result.getWildcards().get("wild1"));
		assertEquals("hellloo",	op1Result.getWildcards().get("wild2"));
		assertEquals("bunnies",	op1Result.getWildcards().get("wild3"));
	}
	
	@Test
	public void testGetAddWildCardOverlap2() {
		RestOperation op1Get 	= new RestOperation(HttpMethod.GET, "/api/monster/hellloo/bunnies/other");
		RestOperation op1 		= new RestOperation(HttpMethod.GET, "/api/{wild1}/{wild2}/{wild3}/other");
		RestOperation op2	 	= new RestOperation(HttpMethod.GET, "/api/{wild1}/hellloo/{wild2}/something");
		
		sut.addFunction(op1, "op1");
		sut.addFunction(op2, "op2");
		
		System.out.println(sut.toString());
		
		ResolvedFunction<String> op1Result = sut.getFunction(op1Get);
		assertEquals("op1", 	op1Result.getFunction());
		assertEquals(3,			op1Result.getWildcards().size());
		assertEquals("monster",	op1Result.getWildcards().get("wild1"));
		assertEquals("hellloo",	op1Result.getWildcards().get("wild2"));
		assertEquals("bunnies",	op1Result.getWildcards().get("wild3"));
		
	}

}


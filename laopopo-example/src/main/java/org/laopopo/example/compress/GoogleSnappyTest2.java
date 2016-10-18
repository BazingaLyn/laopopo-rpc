package org.laopopo.example.compress;

import java.io.IOException;

import org.laopopo.common.serialization.SerializerHolder;
import org.xerial.snappy.Snappy;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time 2016年10月18日
 * @modifytime
 */
public class GoogleSnappyTest2 {
	
	
	public static void main(String[] args) throws IOException {
		
		CompressPojo compressPojo = new CompressPojo();
		
		compressPojo.setI("dasdsadasdfdsfdsfkslshjdhdbsmaiqhdifosjdhfosxhdisdsfdsfdsfhdskjhqwewqewqewqdasdaczczxcxzczxczxcxzcsdfsafdsdfdsfdsfdsfzxccxzczxczxkaudhfiwsolsjdhsjuskidjdhfdhdgwqoqoqkadhdhfdkfldksn");
		compressPojo.setJ(1000);
		compressPojo.setK("1dadfcsrvkslshjdhdbsmaiqhdifosjdhfosxhdiskaudhfiwsolsjdhsjuskcsdfsafdsdfdsfdsfdsfzxccxzczxcidjdhfdhdgwqoqoqkadhdhfdkfldksnxhdiwodhwhfkiwihfsfjgjssaSAzczxczxcxzcsdfsafdsdfdsfdsfdsfzxccxzczxczxkaudhfiwsolsjdhsjuskidjdhfdhdgwqoqo");
		compressPojo.setA("来人往的机场大厅，赵默笙坐audhfiwsolsjdhsjuskcsdfsafdsdf");
		compressPojo.setB("脸上露出一丝惊讶猜出赵默笙是一名摄影audhfiwsolsjdhsjuskcsdfsafdsdf");
		compressPojo.setC("何以玫的面色看起来却开始变audhfiwsolsjdhsjuskcsdfsafdsdf");
		compressPojo.setD("何以玫，何以玫在市内小有名气主持audhfiwsolsjdhsjuskcsdfsafdsdf");
		compressPojo.setE("。赵默笙晚上逛超市购物意audhfiwsolsjdhsjuskcsdfsafdsdf");
		compressPojo.setF("正在超市里面购物正在超市里面购物audhfiwsolsjdhsjuskcsdfsafdsdf");
		compressPojo.setG("少梅是赵默笙的大学同学，成名之后少梅改名为箫筱，箫筱与赵默笙业务合作故意提出停止合audhfiwsolsjdhsjuskcsdfsafdsdf");
		compressPojo.setH("多公司，张主编心知就算她不主动控告箫筱，箫筱也会主动控告杂志社。正如张主编audhfiwsolsjdhsjuskcsdfsafdsdf");
		byte[] bytes = SerializerHolder.serializerImpl().writeObject(compressPojo);
		System.out.println(bytes.length);
		byte[] compressBytes = Snappy.compress(bytes);
		System.out.println(compressBytes.length);
		byte[] uncompressBytes = Snappy.uncompress(compressBytes);
		CompressPojo unCompressPojo = SerializerHolder.serializerImpl().readObject(uncompressBytes, CompressPojo.class);
		
		System.out.println(unCompressPojo.toString());
		
	}
	
	
	
	
	public static class CompressPojo {
		
		private String i;
		
		private Integer j;
		
		private String k;
		
		private String a;
		
		private String b;
		
		private String c;
		
		private String d;
		
		private String e;
		
		private String f;
		
		private String h;
		
		private String g;

		public String getI() {
			return i;
		}

		public void setI(String i) {
			this.i = i;
		}

		public Integer getJ() {
			return j;
		}

		public void setJ(Integer j) {
			this.j = j;
		}

		public String getK() {
			return k;
		}

		public void setK(String k) {
			this.k = k;
		}

		public String getA() {
			return a;
		}

		public void setA(String a) {
			this.a = a;
		}

		public String getB() {
			return b;
		}

		public void setB(String b) {
			this.b = b;
		}

		public String getC() {
			return c;
		}

		public void setC(String c) {
			this.c = c;
		}

		public String getD() {
			return d;
		}

		public void setD(String d) {
			this.d = d;
		}

		public String getE() {
			return e;
		}

		public void setE(String e) {
			this.e = e;
		}

		public String getF() {
			return f;
		}

		public void setF(String f) {
			this.f = f;
		}

		public String getH() {
			return h;
		}

		public void setH(String h) {
			this.h = h;
		}

		public String getG() {
			return g;
		}

		public void setG(String g) {
			this.g = g;
		}

		@Override
		public String toString() {
			return "CompressPojo [i=" + i + ", j=" + j + ", k=" + k + ", a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", e=" + e + ", f=" + f + ", h=" + h
					+ ", g=" + g + "]";
		}
		
	}

}



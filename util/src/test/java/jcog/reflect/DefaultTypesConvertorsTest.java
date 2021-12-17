/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcog.reflect;

import org.junit.jupiter.api.Test;

/**
 *
 * @author user
 */
public class DefaultTypesConvertorsTest {
    

    @Test
    public void showTypes(){
        DefaultTypesConvertors conv = new DefaultTypesConvertors();
        
        System.out.println("default types convertors, to string:");
        for( Class cls : conv.toString.keySet() ){
            System.out.println("  "+cls);
        }
        
        System.out.println("default types convertors, to value:");
        for( Class cls : conv.toValues.keySet() ){
            System.out.println("  "+cls);
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcog.reflect.spi;

import java.util.function.Function;

/**
 * @author Kamnev Georgiy (nt.gocha@gmail.com)
 */
public interface GetTypeConvertor {
    Class getSourceType();

    Class getTargetType();

    Function getConvertor();
}

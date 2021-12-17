package jcog;

/**
 * Indicates involved skill necessary to understand or contribute toward
 * see: Curiosume
 *
 * Each string is of the form:
 *
 *      [ISOlanguage":"]\<WikipediaTag\>[modifiers]
 *
 *      English identifier is assumed by default; other languages can be explicitly specified with the 2-character ISO prefix
 *
 *          es:Metaheurística++
 *          fr:Bug_logiciel_inhabituel--
 *
 * Modifiers:
 *
 *      (none)   just references the topic
 *      +        Teach (ie. requires the help of a teacher)
 *      ++       Teach Expert (ie. requires the help of an expert)
 *      -        Learn (ie. provides help or example to a learner)
 *      --       Learn Beginner (ie. provides help or example to a beginner)
 *
 * Examples:
 *
 *      Graphical_user_interface
 *      Database-
 *      zh:集成测试++
 *
 * TODO IDE plugin w/ Wikipedia browser widget
 */
@Research
public @interface Is {

    String[] value();

}
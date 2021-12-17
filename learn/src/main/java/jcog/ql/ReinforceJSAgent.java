//package jcog.learn.ql;
//
//import jcog.learn.Agent;
//import jdk.nashorn.api.scripting.NashornScriptEngine;
//
//import javax.script.CompiledScript;
//import javax.script.Invocable;
//import javax.script.ScriptContext;
//import javax.script.ScriptEngineManager;
//import java.io.InputStreamReader;
//
///**
// * Created by me on 5/27/16.
// */
//@Deprecated public abstract class ReinforceJSAgent extends Agent {
//    private Invocable js;
//
//    ReinforceJSAgent(int inputs, int actions) {
//        super(inputs, actions);
//        try {
//
//            NashornScriptEngine JS = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("nashorn");
//            JS.eval(
//                new InputStreamReader(ClassLoader.getSystemResourceAsStream("rl.js"))
//            );
//
//            CompiledScript cscript = JS.compile(
//
//                    "var Math = Java.type('java.lang.Math'); " +
//                    "var env = { getNumStates: function() { return " + inputs + "; }, getMaxNumActions: function() { return " + actions + "; } }; " +
//
//                    /*
//                    http:
//                    spec.gamma = 0.9;
//                    spec.epsilon = 0.2;
//                    spec.alpha = 0.005;
//                    spec.experience_add_every = 5;
//                    spec.experience_size = 10000;
//                    spec.learning_steps_per_iteration = 5;
//                    spec.tderror_clamp = 1.0;
//                    spec.num_hidden_units = 100
//                    */
//                    getAgentInitCode(inputs, actions) +
//                    "\nfunction act(i,r) { var a = agent.act(i); agent.learn(r); return a;  } ");
//
//
//
//
//
//
//            cscript.eval(JS.getBindings(ScriptContext.ENGINE_SCOPE));
//            js = (Invocable) cscript.getEngine();
//
//        }catch(Exception e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//       /*
//        var env = {};
//        env.getNumStates = function() { return 8; }
//        env.getMaxNumActions = function() { return 4; }
//
//
//        var spec = { alpha: 0.01 }
//        agent = new RL.DQNAgent(env, spec);
//
//        setInterval(function(){
//          var action = agent.act(s);
//
//          agent.learn(reward);
//        }, 0);
//         */
//
//
//
//
//
//
//
//
//    }
//
//    abstract String getAgentInitCode(int inputs, int actions);
//
//    @Override public int decide(float[] actionFeedback, float prevReward, float... input) {
//        try {
//            Number a = (Number) js.invokeFunction("act", input, prevReward);
//            return a.intValue();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return -1;
//    }
//}

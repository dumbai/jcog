package jcog.classify;

import jcog.Is;

/**
 * Multivariate Adaptive Regression Splines
 * https://projecteuclid.org/download/pdf_1/euclid.aos/1176347963
 * https://github.com/cran/polymars/blob/master/src/polymarsall.c
 **/
@Is("Multivariate_adaptive_regression_splines") public class MARS {

    //TODO translate
    //from: https://github.com/cesar-rojas/mars/blob/master/Csharp/MARS.cs

//    using System;
//    using System.Collections.Generic;
//    using System.Linq;
//    using System.Text;
//    using System.IO;
//    using System.Runtime.InteropServices;
//
//    namespace MARS
//    {
//        public class MARS
//        {
//        [DllImport("EcoEarth.dll",EntryPoint = "Earth", CallingConvention = CallingConvention.Cdecl)]
//            public static extern void Earth(
//                ref double pBestGcv,            // out: GCV of the best model i.e. BestSet columns of bx
//                ref int pnTerms,                // out: max term nbr in final model, after removing lin dep terms
//                ref int iTermCond,              // out: reason we terminated the foward pass
//                bool[] BestSet,          // out: nMaxTerms x 1, indices of best set of cols of bx
//                double[] bx,             // out: nCases x nMaxTerms
//                int[] Dirs,              // out: nMaxTerms x nPreds, -1,0,1,2 for iTerm, iPred
//                double[] Cuts,           // out: nMaxTerms x nPreds, cut for iTerm, iPred
//                double[] Residuals,      // out: nCases x nResp
//                double[] Betas,        // out: nMaxTerms x nResp
//                double[] x,              // in: nCases x nPreds
//                double[] y,                // in: nCases x nResp
//                double[] WeightsArg,       // in: nCases x 1, can be NULL, not yet supported
//        [MarshalAs(UnmanagedType.I4)] [In] int nCases,         // in: number of rows in x and elements in y
//        [MarshalAs(UnmanagedType.I4)] [In] int nResp,          // in: number of cols in y
//            int nPreds,                // in: number of cols in x
//            int nMaxDegree,            // in: Friedman's mi
//            int nMaxTerms,             // in: includes the intercept term
//            double Penalty,            // in: GCV penalty per knot
//            double Thresh,             // in: forward step threshold
//            int nMinSpan,              // in: set to non zero to override internal calculation
//            int nEndSpan,              // in: set to non zero to override internal calculation
//            bool Prune,                // in: do backward pass
//            int nFastK,                // in: Fast MARS K
//            double FastBeta,           // in: Fast MARS ageing coef
//            double NewVarPenalty,      // in: penalty for adding a new variable
//            int[] LinPreds,            // in: nPreds x 1, 1 if predictor must enter linearly
//            double AdjustEndSpan,      // in:
//            bool UseBetaCache,         // in: 1 to use the beta cache, for speed
//            double Trace);             // in: 0 none 1 overview 2 forward 3 pruning 4 more pruning
//
//
//        [DllImport("EcoEarth.dll", EntryPoint = "PredictEarth", CallingConvention = CallingConvention.Cdecl)]
//            public static extern void PredictEarth(
//                double[] y,        // out: vector nResp
//                double[] x,        // in: vector nPreds x 1 of input values
//                bool[] UsedCols,   // in: nMaxTerms x 1, indices of best set of cols of bx
//                int[] Dirs,        // in: nMaxTerms x nPreds, -1,0,1,2 for iTerm, iPred
//                double[] Cuts,     // in: nMaxTerms x nPreds, cut for iTerm, iPred
//                double[] Betas,    // in: nMaxTerms x nResp
//                int nPreds,        // in: number of cols in x
//                int nResp,         // in: number of cols in y
//                int nTerms,
//                int nMaxTerms);
//
//        [DllImport("EcoEarth.dll", EntryPoint = "Predict", CallingConvention = CallingConvention.Cdecl)]
//            public static extern void Predict(
//                double[] y,        // out: vector nResp
//                double[] x,        // in: vector nPreds x 1 of input values
//                int    nPreds,     // in: number of cols in x
//                int    nResp,      // in: number of cols in y
//                int    nMaxTerms);
//
//        [DllImport("EcoEarth.dll", EntryPoint = "FormatEarth", CallingConvention = CallingConvention.Cdecl)]
//            public static extern void FormatEarth(
//            [In] bool[] UsedCols, // in: nMaxTerms x 1, indices of best set of cols of bx
//            [In] int[] Dirs,     // in: nMaxTerms x nPreds, -1,0,1,2 for iTerm, iPred
//            [In] double[] Cuts,     // in: nMaxTerms x nPreds, cut for iTerm, iPred
//            [In] double[] Betas,    // in: nMaxTerms x nResp
//            [In] int nPreds,
//            [In] int nResp,      // in: number of cols in y
//            [In] int nTerms,
//            [In] int nMaxTerms,
//            [In] int nDigits,    // number of significant digits to print
//            [In] double MinBeta);
//
//        [DllImport("EcoEarth.dll", EntryPoint = "FullPredictEarth", CallingConvention = CallingConvention.Cdecl)]
//            public static extern void FullPredictEarth(
//            [In, Out] double[] y_hat,		// out: vector nResp x 1
//            [In] double[] x_pred,		// in: vector nPreds x 1 of input values
//            [In] double[] y_train,		// in: nCases x nResp
//            [In] double[] x_train,		// in: nCases x nPreds
//
//            [In] double[] WeightsArg,   // in: nCases x 1, can be NULL, not yet supported
//            [In] int nCases,           // in: number of rows in x and elements in y
//            [In] int nResp,             // in: number of cols in y
//            [In] int nPreds,            // in: number of cols in x
//            [In] int nOutputs,          // in: number of outputs (# of responses in y_hat)
//            [In] int nMaxDegree,        // in: Friedman's mi
//            [In] int nMaxTerms,         // in: includes the intercept term
//            [In] double Thresh,         // in: forward step threshold
//            [In] int nMinSpan,          // in: set to non zero to override internal calculation
//            [In] int nEndSpan,          // in: set to non zero to override internal calculation
//            [In] bool Prune,            // in: do backward pass
//            [In] int nFastK,            // in: Fast MARS K
//            [In] double FastBeta,       // in: Fast MARS ageing coef
//            [In] double NewVarPenalty,  // in: penalty for adding a new variable
//            [In] double AdjuntEndSpan,  // in:
//            [In] bool UseBetaCache,     // in: 1 to use the beta cache, for speed
//            [In] double Trace);         // in: 0 none 1 overview 2 forward 3 pruning 4 more pruning
//
//        [DllImport("EcoEarth.dll", EntryPoint = "GetRSquareTraining", CallingConvention = CallingConvention.Cdecl)]
//            public static extern double GetRSquareTraining();
//
//        [DllImport("EcoEarth.dll", EntryPoint = "GetGRSquareTraining", CallingConvention = CallingConvention.Cdecl)]
//            public static extern double GetGRSquareTraining();
//
//        [DllImport("EcoEarth.dll", EntryPoint = "GetMAPETraining", CallingConvention = CallingConvention.Cdecl)]
//            public static extern double GetMAPETraining();
//
//            protected int Preds;
//            protected double BestGcv;
//            protected int nTerms;
//            protected int iReason;
//            protected int nMaxTerms = 400;
//
//            protected bool[] BestSet;
//            protected double[] bx;
//            protected int[] Dirs;
//            protected double[] Cuts;
//            protected double[] Residuals;
//            protected double[] Betas;
//
//            protected double _RSquareTesting;
//            protected double _MAPETesting;
//
//            public void Learn(double[][] xData, double[] yData)
//            {
//                int nCases = xData.Length;
//                int nPreds = xData[0].Length;
//                int nResponses = 1; // number of cols in y
//                int nMaxDegree = 2; // Maximum degree of interaction (Friedman’s mi). Default is 1, meaning build an additive model (i.e., no interaction terms).
//                double Penalty = 3; // Generalized Cross Validation (GCV) penalty per knot. Default is if(degree>1) 3 else 2.
//                double ForwardStepThresh = 0.0001; // Forward stepping threshold. Default is 0.001.
//                double Trace = 0; // 3;
//                int nFastK = 20;
//                double FastBeta = 1;
//                double NewVarPenalty = 0;
//                int[] LinPreds = new int[nPreds];
//
//                Preds = nPreds;
//
//                BestGcv = 0;
//                nTerms = 0;
//                iReason = 0;
//
//                // Outputs
//                bx = new double[nCases * nMaxTerms];
//                BestSet = new bool[nMaxTerms];
//                Dirs = new int[nMaxTerms * nPreds];
//                Cuts = new double[nMaxTerms * nPreds];
//                Residuals = new double[nCases * nResponses];
//                Betas = new double[nMaxTerms * nResponses];
//
//
//                double[] x = new double[nCases * nPreds];
//                double[] y = new double[nCases];
//
//                for (int i = 0; i < nPreds; i++)
//                    LinPreds[i] = 0;
//
//                for (int i = 0; i < nCases; i++)
//                    y[i] = yData[i];
//
//                for (int i = 0; i < nCases; i++)
//                {
//                    for (int iPred = 0; iPred < nPreds; iPred++)
//                    {
//                        x[i + iPred * nCases] = xData[i][iPred];
//                    }
//                }
//
//                try
//                {
//                    Earth(
//                            ref BestGcv,
//                            ref nTerms,
//                            ref iReason,
//                            BestSet,
//                            bx,
//                            Dirs,
//                            Cuts,
//                            Residuals,
//                            Betas,
//                            x,
//                            y,
//                            (double[])null,
//                            nCases,
//                            nResponses,
//                            nPreds,
//                            nMaxDegree,
//                            nMaxTerms,
//                            Penalty,
//                            ForwardStepThresh,
//                            (int)1,
//                            (int)1,
//                            true,
//                            nFastK,
//                            FastBeta,
//                            NewVarPenalty,
//                            LinPreds,
//                            (double)0.0,
//                            true,
//                            Trace);
//                }
//                catch (Exception e)
//                {
//                    System.Console.WriteLine(e.ToString());
//                    return;
//                }
//                //FormatEarth(BestSet, Dirs, Cuts, Betas, Preds, nResponses, nTerms, nMaxTerms, 3, 0);
//            }
//
//            public double Predict(Double[] xData)
//            {
//                int nResponses = 1;
//                double[] y = new double[nResponses];
//
//                //System.Console.WriteLine("C# -> nPreds = {0}, nResponses = {1}, nTerms = {2}, nMaxTerms = {3}",
//                //    Preds, nResponses, nTerms, nMaxTerms);
//                Predict(y, xData, Preds, nResponses, nMaxTerms);
//                return y[0];
//            }
//
//            public double[] FullPredict(double[] yTrain, double[][] xTrain, Double[][] xPred, double[] yPred = null)
//            {
//                int nCases = xTrain.Length;
//                int nPreds = xTrain[0].Length;
//                int nOutputs = xPred.Length; // number of responses (outputs) expected
//                int nResponses = 1; // number of cols in y
//                int nMaxDegree = 5; // Maximum degree of interaction (Friedman’s mi). Default is 1, meaning build an additive model (i.e., no interaction terms).
//                double ForwardStepThresh = 0.00001; // Forward stepping threshold. Default is 0.001.
//                double Trace = 0; // 3
//                int nFastK = 20;
//                double FastBeta = 1;
//                double NewVarPenalty = 0.0;
//
//
//                double[] y_hat = new double[nOutputs];
//
//
//                double[] x_train = new double[nCases * nPreds];
//                double[] y_train = new double[nCases];
//
//                double[] x_pred = new double[nOutputs * nPreds];
//
//                for (int i = 0; i < nCases; i++)
//                    y_train[i] = yTrain[i];
//
//                for (int i = 0; i < nCases; i++)
//                {
//                    for (int iPred = 0; iPred < nPreds; iPred++)
//                    {
//                        x_train[i + iPred * nCases] = xTrain[i][iPred];
//                    }
//                }
//
//                for (int iOutput = 0; iOutput < nOutputs; iOutput++)
//                {
//                    for (int iPred = 0; iPred < nPreds; iPred++)
//                        x_pred[iOutput + iPred * nOutputs] = xPred[iOutput][iPred];
//                }
//
//
//                try
//                {
//                    FullPredictEarth(
//                            y_hat,
//                            x_pred,
//                            y_train,
//                            x_train,
//
//                            (double[])null,
//                            nCases,
//                            nResponses,
//                            nPreds,
//                            nOutputs,
//                            nMaxDegree,
//                            nMaxTerms,
//                            ForwardStepThresh,
//                            (int)0,
//                            (int)0,
//                            true,
//                            nFastK,
//                            FastBeta,
//                            NewVarPenalty,
//                            (double)0.0,
//                            true,
//                            Trace);
//                }
//                catch (Exception e)
//                {
//                    System.Console.WriteLine(e.ToString());
//                    return null;
//                }
//
//                _MAPETesting = Double.NaN;
//                _RSquareTesting = Double.NaN;
//                // if yPred is not null, calculate MAPE Testing and R-Square Testing
//                if (yPred != null)
//                {
//                    Double yMean = yPred.Average();
//                    Double RSS = 0.0;
//                    Double TSS = 0.0;
//                    for (Int32 nIndex = 0; nIndex < yPred.Length; nIndex++)
//                    {
//                        RSS += Math.Pow(yPred[nIndex] - y_hat[nIndex], 2.0);
//                        TSS += Math.Pow(yPred[nIndex] - yMean, 2.0);
//                    }
//                    _RSquareTesting = 1 - RSS / TSS;
//                    _MAPETesting = 0;
//                    for (Int32 nIndex = 0; nIndex < y_hat.Length; nIndex++)
//                        _MAPETesting += Math.Abs((yPred[nIndex] - y_hat[nIndex]) / yPred[nIndex]);
//                    _MAPETesting = _MAPETesting / yPred.Length;
//
//                }
//                return y_hat;
//            }
//
//            public double RSquareTraining
//            {
//                get
//                {
//                    return GetRSquareTraining();
//                }
//            }
//
//            public double GRSquareTraining
//            {
//                get
//                {
//                    return GetGRSquareTraining();
//                }
//            }
//
//            public double MAPETraining
//            {
//                get
//                {
//                    return GetMAPETraining();
//                }
//            }
//
//            public double RSquareTesting
//            {
//                get
//                {
//                    return _RSquareTesting;
//                }
//            }
//
//            public double MAPETesting
//            {
//                get
//                {
//                    return _MAPETesting;
//                }
//            }
//        }
//    }

}
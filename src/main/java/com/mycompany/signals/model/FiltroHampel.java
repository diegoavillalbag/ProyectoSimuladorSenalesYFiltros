package com.mycompany.signals.model;

import java.util.Arrays;

/**
 * Filtro de Hampel para remover outliers basándose en la Desviación Absoluta de la Mediana (MAD).
 */
public class FiltroHampel implements Filtro {

    private final int tamanoVentana;
    private final double umbralSigma;

    public FiltroHampel(int tamanoVentana, double umbralSigma) {
        this.tamanoVentana = Math.max(3, tamanoVentana);
        this.umbralSigma = umbralSigma;
    }

    @Override
    public Signal aplicar(Signal senal) {
        double[] ft = senal.getFt();
        int n = ft.length;
        double[] ftFiltrada = new double[n];
        int mitad = tamanoVentana / 2;
        final double CONSTANTE_ESCALA_MAD = 1.4826; 

        for (int i = 0; i < n; i++) {
            int inicio = Math.max(0, i - mitad);
            int fin = Math.min(n - 1, i + mitad);
            int tamanoActual = fin - inicio + 1;

            double[] ventana = new double[tamanoActual];
            System.arraycopy(ft, inicio, ventana, 0, tamanoActual);
            Arrays.sort(ventana);
            
            double mediana = (tamanoActual % 2 == 0) 
                ? (ventana[tamanoActual / 2 - 1] + ventana[tamanoActual / 2]) / 2.0 
                : ventana[tamanoActual / 2];

            double[] desviaciones = new double[tamanoActual];
            for (int j = 0; j < tamanoActual; j++) {
                desviaciones[j] = Math.abs(ft[inicio + j] - mediana);
            }
            Arrays.sort(desviaciones);
            
            double mad = (tamanoActual % 2 == 0) 
                ? (desviaciones[tamanoActual / 2 - 1] + desviaciones[tamanoActual / 2]) / 2.0 
                : desviaciones[tamanoActual / 2];

            double desviacionPuntoActual = Math.abs(ft[i] - mediana);
            if (desviacionPuntoActual > umbralSigma * mad * CONSTANTE_ESCALA_MAD) {
                ftFiltrada[i] = mediana;
            } else {
                ftFiltrada[i] = ft[i];
            }
        }
        return new Signal(senal.getT().clone(), ftFiltrada, false);
    }
    
    public Signal obtenerRespuestaFrecuencia(int N, int fs) {
        // Al ser un filtro adaptativo y NO lineal, su respuesta en frecuencia cambia 
        // dinámicamente punto a punto. No tiene una función de transferencia H(w) fija.
        // Devolvemos una respuesta plana en 1.0
        double[] f = new double[N];
        double[] h = new double[N];
        for (int i = 0; i < N; i++) {
            f[i] = (double) i * fs / N;
            h[i] = 1.0; 
        }
        return new Signal(f, h, false);
    }
    
}
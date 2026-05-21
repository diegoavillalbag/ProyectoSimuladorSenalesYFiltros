package com.mycompany.signals.model;

import java.util.Arrays;

/**
 * Filtro no lineal de Mediana en el dominio del tiempo.
 * Excelente para eliminar ruido impulsivo en ondas cuadradas conservando los bordes.
 */
public class FiltroMediana implements Filtro {

    private final int tamanoVentana;

    public FiltroMediana(int tamanoVentana) {
        // Aseguramos que la ventana sea al menos 3 y preferiblemente impar
        this.tamanoVentana = Math.max(3, tamanoVentana);
    }

    @Override
    public Signal aplicar(Signal senal) {
        double[] ft = senal.getFt();
        double[] t = senal.getT();
        int n = ft.length;
        double[] ftFiltrada = new double[n];
        int mitad = tamanoVentana / 2;

        for (int i = 0; i < n; i++) {
            int inicio = Math.max(0, i - mitad);
            int fin = Math.min(n - 1, i + mitad);
            int tamanoActual = fin - inicio + 1;

            double[] ventana = new double[tamanoActual];
            System.arraycopy(ft, inicio, ventana, 0, tamanoActual);
            
            Arrays.sort(ventana);

            if (tamanoActual % 2 == 0) {
                ftFiltrada[i] = (ventana[tamanoActual / 2 - 1] + ventana[tamanoActual / 2]) / 2.0;
            } else {
                ftFiltrada[i] = ventana[tamanoActual / 2];
            }
        }
        
        return new Signal(t.clone(), ftFiltrada, false);
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

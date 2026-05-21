package com.mycompany.signals.model;

/**
 * Filtro Bilateral 1D adaptativo y no lineal.
 * Combina un núcleo espacial (distancia en tiempo) y un núcleo de rango (distancia en amplitud).
 * Diseñado para aplanar por completo el ruido en zonas constantes sin deformar los bordes verticales.
 */
public class FiltroBilateral implements Filtro {

    private final int tamanoVentana;
    private final double umbralSigma;

    /**
     * Constructor del filtro bilateral.
     * @param tamanoVentana Número de puntos de la ventana (debe ser impar, ej. 21, 31).
     * @param umbralSigma Tolerancia al ruido (mayor que la amplitud del ruido, menor que el salto de la onda).
     */
    public FiltroBilateral(int tamanoVentana, double umbralSigma) {
        this.tamanoVentana = tamanoVentana;
        this.umbralSigma = umbralSigma;
    }

    @Override
    public Signal aplicar(Signal senalIn) {
        // 1. Extraer los vectores de tiempo y amplitud
        double[] entrada = senalIn.getFt();
        double[] t = senalIn.getT();
        
        int n = entrada.length;
        double[] salida = new double[n];
        int radio = tamanoVentana / 2;

        // 2. Calcular divisores para las funciones gaussianas (Precalculo)
        double divisorGaussIntensidad = 2.0 * umbralSigma * umbralSigma;
        if (divisorGaussIntensidad == 0.0) {
            divisorGaussIntensidad = 0.0001; // Protección básica contra división por cero
        }

        // El sigma espacial se autocalcula dinámicamente según el radio de la ventana
        double sigmaEspacio = radio > 0 ? radio / 2.0 : 1.0;
        double divisorGaussEspacio = 2.0 * sigmaEspacio * sigmaEspacio;

        // 3. Procesar la señal muestra por muestra mediante el núcleo combinado
        for (int i = 0; i < n; i++) {
            double sumaNumerador = 0.0;
            double sumaDenominador = 0.0;

            for (int j = -radio; j <= radio; j++) {
                int idx = i + j;

                // Validar límites del arreglo para evitar desbordamientos en los extremos
                if (idx >= 0 && idx < n) {
                    // Componente de Intensidad: Atenúa vecinos con amplitudes muy lejanas (bordes)
                    double diffAmp = entrada[idx] - entrada[i];
                    double pesoRango = Math.exp(-(diffAmp * diffAmp) / divisorGaussIntensidad);

                    // Componente Espacial: Da mayor peso a las muestras físicamente más cercanas
                    double pesoEspacio = Math.exp(-(j * j) / divisorGaussEspacio);

                    // Ponderación final combinada
                    double pesoTotal = pesoRango * pesoEspacio;

                    sumaNumerador += entrada[idx] * pesoTotal;
                    sumaDenominador += pesoTotal;
                }
            }

            // 4. Guardar la muestra filtrada (si el denominador es cero se preserva el valor original)
            salida[i] = (sumaDenominador == 0.0) ? entrada[i] : (sumaNumerador / sumaDenominador);
        }

        return new Signal(t, salida, false);
    }


    public Signal obtenerRespuestaFrecuencia(int N, int fs) {
        // Al ser un filtro adaptativo y no lineal, su respuesta en frecuencia 
        // cambia dinámicamente punto a punto. No tiene una función de transferencia H(w) fija.
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
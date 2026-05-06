package com.mycompany.signals.model;

public class GeneradorFiltros {

    /**
     * Crea un filtro Pasa-Bajos Butterworth de 1er orden.
     * @param fc Frecuencia de corte en Hz.
     * @param fs Frecuencia de muestreo en Hz.
     * @return Un objeto FiltroDiferencias configurado.
     */
    public static Filtro crearPasaBajos(double fc, double fs) {
        // La magia matemática: Transformación Bilineal
        // Convertimos la frecuencia de Hz a radianes y normalizamos
        double gamma = Math.tan(Math.PI * fc / fs);
        double denominador = 1.0 + gamma;

        // Coeficientes para la ecuación de diferencias
        double b0 = gamma / denominador;
        double b1 = gamma / denominador;
        double a1 = (gamma - 1.0) / denominador;

        double[] b = {b0, b1};
        double[] a = {1.0, a1}; // El 1.0 es a0

        return new FiltroDiferencias(b, a);
    }

    /**
     * Crea un filtro Pasa-Altos Butterworth de 1er orden.
     * @param fc Frecuencia de corte en Hz.
     * @param fs Frecuencia de muestreo en Hz.
     * @return Devuelve un FiltroDiferencias
     */
    public static Filtro crearPasaAltos(double fc, double fs) {
        double gamma = Math.tan(Math.PI * fc / fs);
        double denominador = 1.0 + gamma;

        double b0 = 1.0 / denominador;
        double b1 = -1.0 / denominador;
        double a1 = (gamma - 1.0) / denominador;

        double[] b = {b0, b1};
        double[] a = {1.0, a1};

        return new FiltroDiferencias(b, a);
    }
}

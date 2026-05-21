package com.mycompany.signals.model;

/**
 * Fábrica de filtros IIR discretos (Butterworth y pasa-banda)
 * mediante transformación bilineal y coeficientes b, a.
 */
public class GeneradorFiltros {

    /**
     * Crea un filtro Pasa-Bajos Butterworth de 1er orden.
     * @param fc Frecuencia de corte en Hz.
     * @param fs Frecuencia de muestreo en Hz.
     * @return Devuelve un FiltroDiferencias
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

    /**
     * Crea un filtro Pasa-Bajos Butterworth de 2do orden (Biquad).
     * Corte más abrupto (-40 dB/década) y respuesta plana en la banda de paso.
     * @param fc Frecuencia de corte en Hz.
     * @param fs Frecuencia de muestreo en Hz.
     * @return Un objeto FiltroDiferencias configurado.
     */
    public static Filtro crearPasaBajos2doOrden(double fc, double fs) {
        // 1. Calcular la frecuencia angular normalizada usando la tangente (Pre-warping)
        double omega = 2.0 * Math.PI * fc / fs;
        double K = Math.tan(omega / 2.0);

        // Factor de calidad para Butterworth de 2do orden
        double Q = 1.0 / Math.sqrt(2.0); // aprox 0.7071

        // 2. Denominador común (equivale a a0 antes de normalizar)
        double K2 = K * K;
        double denominador = K2 + (K / Q) + 1.0;

        // 3. Calcular coeficientes normalizados
        double b0 = K2 / denominador;
        double b1 = 2.0 * K2 / denominador;
        double b2 = K2 / denominador;

        double a1 = 2.0 * (K2 - 1.0) / denominador;
        double a2 = (K2 - (K / Q) + 1.0) / denominador;

        // 4. Empaquetar arreglos
        double[] b = {b0, b1, b2};
        double[] a = {1.0, a1, a2}; // a0 es siempre 1.0

        return new FiltroDiferencias(b, a);
    }

    /**
     * Crea un filtro Pasa-Altos Butterworth de 2do orden (Biquad).
     * Elimina frecuencias bajas de forma abrupta y maximiza la ganancia en Nyquist.
     * @param fc Frecuencia de corte en Hz.
     * @param fs Frecuencia de muestreo en Hz.
     * @return Un objeto FiltroDiferencias configurado.
     */
    public static Filtro crearPasaAltos2doOrden(double fc, double fs) {
        // 1. Calcular la frecuencia angular normalizada usando la tangente (Pre-warping)
        double omega = 2.0 * Math.PI * fc / fs;
        double K = Math.tan(omega / 2.0);

        // Factor de calidad para Butterworth de 2do orden
        double Q = 1.0 / Math.sqrt(2.0);

        // 2. Denominador común (equivale a a0 antes de normalizar)
        double K2 = K * K;
        double denominador = K2 + (K / Q) + 1.0;

        // 3. Coeficientes específicos para Pasa-Altos
        double b0 = 1.0 / denominador;
        double b1 = -2.0 / denominador;
        double b2 = 1.0 / denominador;

        double a1 = 2.0 * (K2 - 1.0) / denominador;
        double a2 = (K2 - (K / Q) + 1.0) / denominador;

        // 4. Empaquetar arreglos
        double[] b = {b0, b1, b2};
        double[] a = {1.0, a1, a2};

        return new FiltroDiferencias(b, a);
    }

    /**
     * Crea un filtro Pasa-Banda de 2do orden (Biquad).
     * Sintoniza una frecuencia central específica atenuando todo lo demás a los lados.
     * @param f0 Frecuencia central de sintonía en Hz.
     * @param Q Factor de Calidad (Determina qué tan estrecha es la banda de paso).
     * @param fs Frecuencia de muestreo en Hz.
     * @return Un objeto FiltroDiferencias configurado.
     */
    public static Filtro crearPasaBanda(double f0, double Q, double fs) {
        // 1. Calcular la frecuencia angular central normalizada (Pre-warping)
        double omega0 = 2.0 * Math.PI * f0 / fs;

        // Términos intermedios de la Transformación Bilineal
        double sen0 = Math.sin(omega0);
        double cos0 = Math.cos(omega0);
        double alpha = sen0 / (2.0 * Q);

        // 2. Denominador común para normalizar (equivale a a0)
        double denominador = 1.0 + alpha;

        // 3. Calcular coeficientes normalizados para el pasa-banda (Ganancia pico constante = 0 dB)
        double b0 = alpha / denominador;
        double b1 = 0.0;
        double b2 = -alpha / denominador;

        double a1 = -2.0 * cos0 / denominador;
        double a2 = (1.0 - alpha) / denominador;

        // 4. Empaquetar arreglos para FiltroDiferencias
        double[] b = {b0, b1, b2};
        double[] a = {1.0, a1, a2}; // a0 siempre es 1.0

        return new FiltroDiferencias(b, a);
    }
    
    /**
     * Crea un filtro Rechaza-Banda (Notch) de 2do orden (Biquad).
     * Sintoniza una frecuencia específica para eliminarla (ej. ruido de 50Hz o 60Hz), 
     * dejando pasar el resto de las frecuencias casi sin alteraciones.
     * @param f0 Frecuencia central a rechazar (eliminar) en Hz.
     * @param Q Factor de Calidad (Determina qué tan estrecha es la "muesca". Un Q alto = corte muy fino).
     * @param fs Frecuencia de muestreo en Hz.
     * @return Un objeto FiltroDiferencias configurado.
     */
    public static Filtro crearRechazaBanda(double f0, double Q, double fs) {
        // 1. Calcular la frecuencia angular central normalizada
        double omega0 = 2.0 * Math.PI * f0 / fs;

        // Términos intermedios de la transformación
        double sen0 = Math.sin(omega0);
        double cos0 = Math.cos(omega0);
        double alpha = sen0 / (2.0 * Q);

        // 2. Denominador común para normalizar (equivale a a0)
        double denominador = 1.0 + alpha;

        // 3. Calcular coeficientes normalizados para el rechaza-banda
        double b0 = 1.0 / denominador;
        double b1 = -2.0 * cos0 / denominador;
        double b2 = 1.0 / denominador;

        double a1 = -2.0 * cos0 / denominador;
        double a2 = (1.0 - alpha) / denominador;

        // 4. Empaquetar arreglos para FiltroDiferencias
        double[] b = {b0, b1, b2};
        double[] a = {1.0, a1, a2}; // a0 siempre es 1.0

        return new FiltroDiferencias(b, a);
    }
    
    /**
     * Crea un filtro de Media Móvil Simple (FIR).
     * Suaviza la señal promediando N muestras consecutivas.
     * @param nPuntos Número de puntos de la ventana (ej. 5, 10, 20).
     * @return Un objeto FiltroDiferencias configurado.
     */
    public static Filtro crearMediaMovil(int nPuntos) {
        if (nPuntos < 2) nPuntos = 2; // Protección básica
        
        double[] b = new double[nPuntos];
        double coeficiente = 1.0 / nPuntos;
        
        for (int i = 0; i < nPuntos; i++) {
            b[i] = coeficiente;
        }

        double[] a = {1.0}; // Filtro FIR, sin feedback

        return new FiltroDiferencias(b, a);
    }
    
    /**
     * Crea un filtro de suavizado Savitzky-Golay de 5 puntos (Polinomio Cuadrático/Cúbico).
     * Ideal para suavizar señales ruidosas conservando la forma y la altura de los picos.
     * @return Un objeto FiltroDiferencias configurado.
     */
    public static Filtro crearSavitzkyGolay5Puntos() {
        // Coeficientes precalculados para N=5, grado=2 o 3
        // La suma de los numeradores es 35 (-3 + 12 + 17 + 12 - 3 = 35)
        double[] b = {
            -3.0 / 35.0,
            12.0 / 35.0,
            17.0 / 35.0,
            12.0 / 35.0,
            -3.0 / 35.0
        };

        // Al ser un filtro FIR, no hay coeficientes recursivos (feedback)
        double[] a = {1.0};

        return new FiltroDiferencias(b, a);
    }

    /**
     * Crea un filtro de suavizado Savitzky-Golay de 7 puntos (Polinomio Cuadrático/Cúbico).
     * Proporciona un suavizado más agresivo que el de 5 puntos.
     * @return Un objeto FiltroDiferencias configurado.
     */
    public static Filtro crearSavitzkyGolay7Puntos() {
        // Coeficientes precalculados para N=7, grado=2 o 3
        // La suma de los numeradores es 21 (-2 + 3 + 6 + 7 + 6 + 3 - 2 = 21)
        double[] b = {
            -2.0 / 21.0,
             3.0 / 21.0,
             6.0 / 21.0,
             7.0 / 21.0,
             6.0 / 21.0,
             3.0 / 21.0,
            -2.0 / 21.0
        };

        double[] a = {1.0};

        return new FiltroDiferencias(b, a);
    }
    
    /**
     * Crea un filtro Savitzky-Golay de 11 puntos (Polinomio Cuadrático/Cúbico).
     * Nivel de Suavizado: MEDIO.
     * @return Un objeto FiltroDiferencias configurado.
     */
    public static Filtro crearSavitzkyGolay11Puntos() {
        // La suma de los numeradores es 429
        double[] b = {
            -36.0 / 429.0,
              9.0 / 429.0,
             44.0 / 429.0,
             69.0 / 429.0,
             84.0 / 429.0,
             89.0 / 429.0, // Punto central
             84.0 / 429.0,
             69.0 / 429.0,
             44.0 / 429.0,
              9.0 / 429.0,
            -36.0 / 429.0
        };

        double[] a = {1.0};
        return new FiltroDiferencias(b, a);
    }

    /**
     * Crea un filtro Savitzky-Golay de 15 puntos (Polinomio Cuadrático/Cúbico).
     * Nivel de Suavizado: FUERTE.
     * @return Un objeto FiltroDiferencias configurado.
     */
    public static Filtro crearSavitzkyGolay15Puntos() {
        // La suma de los numeradores es 1105
        double[] b = {
            -78.0 / 1105.0,
            -13.0 / 1105.0,
             42.0 / 1105.0,
             87.0 / 1105.0,
            122.0 / 1105.0,
            147.0 / 1105.0,
            162.0 / 1105.0,
            167.0 / 1105.0, // Punto central
            162.0 / 1105.0,
            147.0 / 1105.0,
            122.0 / 1105.0,
             87.0 / 1105.0,
             42.0 / 1105.0,
            -13.0 / 1105.0,
            -78.0 / 1105.0
        };

        double[] a = {1.0};
        return new FiltroDiferencias(b, a);
    }

    /**
     * Crea un filtro Savitzky-Golay de 21 puntos (Polinomio Cuadrático/Cúbico).
     * Nivel de Suavizado: MUY FUERTE (Agresivo, para ruido extremo).
     * @return Un objeto FiltroDiferencias configurado.
     */
    public static Filtro crearSavitzkyGolay21Puntos() {
        // La suma de los numeradores es 3059
        double[] b = {
            -171.0 / 3059.0,
             -76.0 / 3059.0,
               9.0 / 3059.0,
              84.0 / 3059.0,
             149.0 / 3059.0,
             204.0 / 3059.0,
             249.0 / 3059.0,
             284.0 / 3059.0,
             309.0 / 3059.0,
             324.0 / 3059.0,
             329.0 / 3059.0, // Punto central
             324.0 / 3059.0,
             309.0 / 3059.0,
             284.0 / 3059.0,
             249.0 / 3059.0,
             204.0 / 3059.0,
             149.0 / 3059.0,
              84.0 / 3059.0,
               9.0 / 3059.0,
             -76.0 / 3059.0,
            -171.0 / 3059.0
        };

        double[] a = {1.0};
        return new FiltroDiferencias(b, a);
    }
    
    /**
     * Crea un filtro no lineal de Mediana.
     * Ideal para remover ruido de señales cuadradas preservando los bordes abruptos.
     * @param tamanoVentana Número impar de muestras a considerar (ej. 3, 5, 9).
     * @return Un objeto FiltroMediana.
     */
    public static Filtro crearFiltroMediana(int tamanoVentana) {
        return new FiltroMediana(tamanoVentana);
    }

    /**
     * Crea un filtro de Hampel.
     * Actúa como un detector de anomalías, limpiando solo los picos atípicos.
     * @param tamanoVentana Tamaño de la vecindad.
     * @param umbralSigma Tolerancia (típicamente 3.0).
     * @return Un objeto FiltroHampel.
     */
    public static Filtro crearFiltroHampel(int tamanoVentana, double umbralSigma) {
        return new FiltroHampel(tamanoVentana, umbralSigma);
    }
    
    public static Filtro crearFiltroBilateral(int tamanoVentana, double umbralSigma) {
        return new FiltroBilateral(tamanoVentana, umbralSigma);
    }

}

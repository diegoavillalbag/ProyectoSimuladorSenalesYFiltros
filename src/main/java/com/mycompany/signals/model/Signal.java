package com.mycompany.signals.model;

public class Signal {

    private final double[] t;
    private final double[] ft;

    /**
     * Constructor de la Señal.
     * @param t Arreglo de tiempo.
     * @param ft Arreglo de amplitudes (señal real). El tamaño DEBE ser potencia de 2.
     */
    public Signal(double[] t, double[] ft) {
        if (t.length != ft.length) {
            throw new IllegalArgumentException("Los arreglos t y f(t) deben tener la misma longitud.");
        }
        
        int n = ft.length;
        if ((n & (n - 1)) != 0 || n == 0) {
            throw new IllegalArgumentException("El tamaño de la señal f(t) debe ser una potencia de 2.");
        }
        
        this.t = t;
        this.ft = ft;
    }
    
    // Constructor sin validacion para poder generar las FFT
    public Signal(double[] t, double[] ft, boolean verif) {
        if (t.length != ft.length) {
            throw new IllegalArgumentException("Los arreglos t y f(t) deben tener la misma longitud.");
        }
        
        int n = ft.length;
        if (((n & (n - 1)) != 0 || n == 0) && verif) {
            throw new IllegalArgumentException("El tamaño de la señal f(t) debe ser una potencia de 2.");
        }
        
        this.t = t;
        this.ft = ft;
    }

      /**
     * Genera una onda senoidal pura.
     * Fórmula: f(t) = A * sin(2 * pi * f * t)
     */
    public static Signal crearSenoidal(double amplitud, double frecuenciaHz, double fs, int N) {
        double[] t = new double[N];
        double[] ft = new double[N];
        double dt = 1.0 / fs;

        for (int i = 0; i < N; i++) {
            t[i] = i * dt;
            ft[i] = amplitud * Math.sin(2.0 * Math.PI * frecuenciaHz * t[i]);
        }
        return new Signal(t, ft);
    }

    /**
     * Genera una onda cuadrada.
     * Utiliza la función signo (signum) sobre una onda senoidal.
     * @param fs Frecuencia de Muestreo
     */
    public static Signal crearCuadrada(double amplitud, double frecuenciaHz, double fs, int N) {
        double[] t = new double[N];
        double[] ft = new double[N];
        double dt = 1.0 / fs;

        for (int i = 0; i < N; i++) {
            t[i] = i * dt;
            // Math.signum devuelve -1.0 o 1.0 dependiendo del signo del seno
            ft[i] = amplitud * Math.signum(Math.sin(2.0 * Math.PI * frecuenciaHz * t[i]));
        }
        return new Signal(t, ft);
    }

    /**
     * Genera una señal de corriente continua (DC).
     * Fórmula: f(t) = A
     */
    public static Signal crearContinua(double amplitud, double fs, int N) {
        double[] t = new double[N];
        double[] ft = new double[N];
        double dt = 1.0 / fs;

        for (int i = 0; i < N; i++) {
            t[i] = i * dt;
            ft[i] = amplitud;
        }
        return new Signal(t, ft);
    }

    /**
     * Suma dos señales (útil para mezclar ondas).
     * Ambas señales deben tener la misma longitud.
     * @param otraSenal Señal de entrada
     * @return Señal de salida
     */
    public Signal sumar(Signal otraSenal) {
        if (this.ft.length != otraSenal.ft.length) {
            throw new IllegalArgumentException("Las señales deben tener el mismo tamaño para sumarse.");
        }
        
        int n = this.ft.length;
        double[] nuevaFt = new double[n];
        
        for (int i = 0; i < n; i++) {
            nuevaFt[i] = this.ft[i] + otraSenal.ft[i];
        }
        
        // Reutilizamos el eje temporal de la señal actual
        return new Signal(this.t.clone(), nuevaFt); 
    }
    
    /**
     * Calcula la FFT optimizada para señales reales.
     * @return Arreglo de tamaño (N/2 + 1) con las magnitudes normalizadas de las frecuencias positivas.
     */
    private double[] calcularMagnitudesFFT() {
        int n = ft.length;
        int m = 31 - Integer.numberOfLeadingZeros(n);

        // Arreglos internos para el cálculo in-place
        double[] real = new double[n];
        double[] imag = new double[n];

        // Fase de Inversión de Bits (Bit-Reversal)
        for (int i = 0; i < n; i++) {
            int rev = Integer.reverse(i) >>> (32 - m);
            real[rev] = ft[i];
            // imag[rev] ya es 0.0
        }

        // Algoritmo Cooley-Tukey Iterativo
        for (int s = 1; s <= m; s++) {
            int m_s = 1 << s;
            int half_m_s = m_s / 2;
            
            double theta = -2.0 * Math.PI / m_s;
            double w_m_r = Math.cos(theta);
            double w_m_i = Math.sin(theta);

            for (int k = 0; k < n; k += m_s) {
                double w_r = 1.0;
                double w_i = 0.0;
                
                for (int j = 0; j < half_m_s; j++) {
                    int u_idx = k + j;
                    int t_idx = k + j + half_m_s;

                    double t_r = w_r * real[t_idx] - w_i * imag[t_idx];
                    double t_i = w_r * imag[t_idx] + w_i * real[t_idx];

                    double u_r = real[u_idx];
                    double u_i = imag[u_idx];

                    real[t_idx] = u_r - t_r;
                    imag[t_idx] = u_i - t_i;
                    real[u_idx] = u_r + t_r;
                    imag[u_idx] = u_i + t_i;

                    double next_w_r = w_r * w_m_r - w_i * w_m_i;
                    double next_w_i = w_r * w_m_i + w_i * w_m_r;
                    w_r = next_w_r;
                    w_i = next_w_i;
                }
            }
        }

        // Extraer y normalizar solo las frecuencias reales positivas
        int numFrequencies = (n / 2) + 1;
        double[] magnitudes = new double[numFrequencies];

        for (int i = 0; i < numFrequencies; i++) {
            double mag = Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
            if (i == 0 || i == numFrequencies - 1) {
                magnitudes[i] = mag / n;
            } else {
                magnitudes[i] = (2.0 * mag) / n;
            }
        }

        return magnitudes;
    }

    /**
     * Utiliza el vector de tiempo 't' para calcular a cuántos Hertz (Hz)
     * corresponde cada índice del arreglo devuelto por calcularMagnitudesFFT().
     * @return Arreglo con los valores en Hz para cada Bin de la FFT.
     */
    private double[] calcularEjeFrecuencias() {
        int n = ft.length;
        int numFrequencies = (n / 2) + 1;
        double[] hz = new double[numFrequencies];
        
        // Asumimos un muestreo uniforme: dt = t[1] - t[0]
        double dt = t[1] - t[0]; 
        double fs = 1.0 / dt; // Frecuencia de muestreo (Sample Rate)

        for (int k = 0; k < numFrequencies; k++) {
            hz[k] = (k * fs) / n;
        }

        return hz;
    }
    
    public Signal calcularFFT(){
        return new Signal(this.calcularEjeFrecuencias(), this.calcularMagnitudesFFT(), false);
    }

    // --- Getters básicos ---
    public double[] getT() { return t; }
    public double[] getFt() { return ft; }
}
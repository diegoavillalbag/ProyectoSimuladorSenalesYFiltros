package com.mycompany.signals.UI.controllers;

import com.mycompany.signals.model.Filtro;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.GridPane;
import javafx.scene.chart.LineChart;
import javafx.event.ActionEvent;

// Importar los paquetes de señales y filtros
import com.mycompany.signals.model.Signal;
import com.mycompany.signals.model.FiltroDiferencias;
import com.mycompany.signals.model.GeneradorFiltros;
import com.mycompany.signals.UI.GeneradorGraficasFX;

public class MainViewController implements Initializable {

    // Constantes del programa
    private final int N = 2048; // Cantidad de puntos de señales y fft
    private final int n = 4; // Reduccion para no graficar todos los puntos
    private final int fs = 2048; 
    
    // Variables del programa
    private Signal senalEntrada;
    private Signal senalEntradaFFT;
    private Signal senalSalida;
    private Signal senalSalidaFFT;
    private Filtro filtroActual;
    
    // Objetos de la interfaz
    GeneradorGraficasFX Graficador = new GeneradorGraficasFX();
    
    // Graficos
    @FXML private GridPane SignalGrid;
    
    
    // Menu señal de entrada
    @FXML private ChoiceBox<String> SignalSelector;
    @FXML private TextField SignalFreq;
    @FXML private TextField SignalAmp;
    
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Al iniciar la interfaz se carga una señal con ruido por defecto
        // Se aplica un filtro paso bajo de 1er orden y se muestra en pantalla
        
        int signalAmp = 1;
        int signalFreq = 10;
        double noiseAmp = 0.15;
        int noiseFreq = 500;
        
        senalEntrada = Signal.crearSenoidal(signalAmp, signalFreq, fs, N);
        Signal ruidoEntrada = Signal.crearSenoidal(noiseAmp, noiseFreq, fs, N);
        senalEntrada = ruidoEntrada.sumar(senalEntrada);
        
        senalEntradaFFT = senalEntrada.calcularFFT();
        
        filtroActual = GeneradorFiltros.crearPasaBajos(100, fs);
        
        senalSalida = filtroActual.aplicar(senalEntrada);
        senalSalidaFFT = senalSalida.calcularFFT();
        
        this.actualizarGraficas();
        
    }    
    
    public void actualizarGraficas(){
        // 0. Objeto creador de graficas
        
        
        // 1. Limpiar la cuadrícula por si hay gráficos viejos
        SignalGrid.getChildren().clear();

        // 2. Obtener los gráficos de tus métodos
        LineChart<Number, Number> chart1 = Graficador.crearGrafica("Grafico 1", "serie 1", senalEntrada, n);
        LineChart<Number, Number> chart2 = Graficador.crearGraficaXLog("Grafico 2", "serie 2", senalEntradaFFT);
        LineChart<Number, Number> chart3 = Graficador.crearGrafica("Grafico 3", "serie 3", senalSalida, n);
        LineChart<Number, Number> chart4 = Graficador.crearGraficaXLog("Grafico 4", "serie 4", senalSalidaFFT);

        // 3. Posicionar en el Grid (Columna, Fila)
        // Fila 0
        SignalGrid.add(chart1, 0, 0); // Arriba Izquierda
        SignalGrid.add(chart2, 1, 0); // Arriba Derecha
        
        // Fila 1
        SignalGrid.add(chart3, 0, 1); // Abajo Izquierda
        SignalGrid.add(chart4, 1, 1); // Abajo Derecha
    }
    
    // Metodos OnAction
    @FXML
    private void SignalApplyOnAction(ActionEvent event) { 
        // Tu lógica aquí
    }
    
    @FXML
    private void SignalDeleteOnAction(ActionEvent event) { 
        // Tu lógica aquí
    }
    
}

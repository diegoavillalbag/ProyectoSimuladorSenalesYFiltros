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
import javafx.scene.chart.XYChart;

// Importar los paquetes de señales y filtros
import com.mycompany.signals.model.Signal;
import com.mycompany.signals.model.GeneradorFiltros;
import com.mycompany.signals.UI.GeneradorGraficasFX;
import com.mycompany.signals.model.GeneradorRuido;


public class MainViewController implements Initializable {

    // Constantes del programa
    private final int N = 4096; // Cantidad de puntos de señales y fft
    private final int n = 50; // Reduccion para no graficar todos los puntos
    private final int fs = 32768; 
    
    // Variables del programa
    private Signal senalEntrada;
    private Signal senalEntradaFFT;
    private Signal ruidoEntrada; // Aqui se guarda el ruido de la entrada
    private Signal respuestaFiltro;
    private Signal senalSalida;
    private Signal senalSalidaFFT;
    private Filtro filtroActual; // posiblemente se deba modificar a un arreglo de filtros luego
    
    // Objetos de la interfaz
    GeneradorGraficasFX Graficador = new GeneradorGraficasFX(); // Objeto de tipo graficador para manejar los graficos
    
    // Graficos
    @FXML private GridPane SignalGrid;
    
    
    // Menu señal de entrada
    @FXML private ChoiceBox<String> SignalSelector; // Para seleccionar el tipo de señal a usar
    // Definir las opciones
    private final String[] opcionesTipoSenalEntrada = {"Senoidal", "Cuadrada"};
    @FXML private TextField SignalFreq; // Entrada de la frecuencia de la señal de entrada
    @FXML private TextField SignalAmp; // Entrada de la amplitud de la señal de entrada
    
    // Menu ruido
    @FXML private ChoiceBox<String> NoiseSelector; 
    private final String[] opcionesTipoRuido = {"Ruido Aleatorio Plano", "Ruido Aleatorio Porcentual", 
                                                "Ruido Interferencia (Senoidal)"};
    @FXML private TextField NoiseFreq; // Entrada de la frecuencia del ruido
    @FXML private TextField NoiseAmp; // Entrada de la amplitud del ruido
    @FXML private TextField NoisePrctg; // Entrada del porcentaje de error
    
    // Menu filtros
    @FXML private ChoiceBox<String> FilterSelector;
    // Definir las opciones
    private final String[] opcionesTipoFiltro = {"Pasa Bajos 1er Orden", "Pasa Altos 1er Orden", 
                                                "Pasa Bajos 2do Orden", "Pasa Altos 2do Orden",
                                                "Pasa Banda 2do Orden"};
    @FXML private TextField FilterFc; // Frecuencia de corte/central del filtro
    @FXML private TextField FilterQ; // Q del filtro (solo para paso banda)
    
    
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Al iniciar la interfaz se carga una señal con ruido por defecto
        // Se aplica un filtro paso banda y se muestra en pantalla
        
        double signalAmp = 1;
        double signalFreq = 1000;
        senalEntrada = Signal.crearSenoidal(signalAmp, signalFreq, fs, N);
        
        double noiseAmp = 0.3;
        double noiseFreq = 200;
        double noisePrctg = 0.15;
        ruidoEntrada = GeneradorRuido.aplicarRuidoPorcentual(senalEntrada, noisePrctg);
        
        double filterFc = 1000;
        double filterQ = 10;
        filtroActual = GeneradorFiltros.crearPasaBajos(filterFc, fs);

        this.actualizarGraficas();
        
        // Al inicializar se deben cargar los datos necesarios dentro de la interfaz

        // Datos sector señal de entrada
        
        // Cargar las opciones al ChoiceBox de señal
        SignalSelector.getItems().addAll(opcionesTipoSenalEntrada);

        // Opcion por defecto
        SignalSelector.setValue(opcionesTipoSenalEntrada[0]);
        
        // Cargar datos a los textfield
        SignalFreq.setText(String.valueOf(signalFreq));
        SignalAmp.setText(String.valueOf(signalAmp));
        
        // Datos sector ruido
        
        // Carrgar las opciones al choicebox de ruido
        NoiseSelector.getItems().addAll(opcionesTipoRuido);
        
        // Opcion por defecto (Ruido porcentual)
        NoiseSelector.setValue(opcionesTipoRuido[1]);
        
        // Cargar datos a los textfield
        NoiseFreq.setText(String.valueOf(noiseFreq));
        NoiseAmp.setText(String.valueOf(noiseAmp));
        NoisePrctg.setText(String.valueOf(noisePrctg));
        
        
        // Datos sector filtros
        
        // Cargar las opciones al choicebox de filtro
        FilterSelector.getItems().addAll(opcionesTipoFiltro);
        
        // Opcion por defecto
        FilterSelector.setValue(opcionesTipoFiltro[0]);
        
        // Cargar datos a los textfield
        FilterFc.setText(String.valueOf(filterFc));
        FilterQ.setText(String.valueOf(filterQ));
        
        // Listener para activar y desactivar casillas de la interfaz
        // Ruido
        // Desactivados por defecto
        NoiseFreq.setDisable(true);
        NoiseAmp.setDisable(true);
        NoisePrctg.setDisable(false);
        
        // Listener
        NoiseSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // Si la nueva opción seleccionada es "Ruido Aleatorio Porcentual"
            if (newValue != null && newValue.equals("Ruido Aleatorio Porcentual")) {
                NoiseFreq.setDisable(true);
                NoiseAmp.setDisable(true);
                NoisePrctg.setDisable(false);
            } else if (newValue != null && newValue.equals("Ruido Aleatorio Plano")) {
                NoiseFreq.setDisable(true);
                NoiseAmp.setDisable(false);
                NoisePrctg.setDisable(true);
            } else {
                NoiseFreq.setDisable(false);
                NoiseAmp.setDisable(false);
                NoisePrctg.setDisable(true);
            }
        });
        
        // Filtros
        // Estado inicial Q desactivado porque la opción por defecto no es "Pasa Banda"
        FilterQ.setDisable(true);
        
        // Listener para detectar cambios en el menú desplegable de los filtros
        FilterSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // Si la nueva opción seleccionada es "Pasa Banda 2do Orden", habilitar la casilla Q
            if (newValue != null && newValue.equals("Pasa Banda 2do Orden")) {
                FilterQ.setDisable(false);
            } else {
                // Para cualquier otro filtro, deshabilitar la casilla Q
                FilterQ.setDisable(true);
            }
        });
        
    }    
    
    public void actualizarGraficas(){
        
        // 1. Limpiar la cuadrícula por si hay gráficos viejos
        SignalGrid.getChildren().clear();

        // 2. Calcular datos de grafico
        Signal entradaConRuido = senalEntrada.sumar(ruidoEntrada);
        senalEntradaFFT = entradaConRuido.calcularFFT(); // Calcula la FFT de la mezcla real
        senalSalida = filtroActual.aplicar(entradaConRuido);
        senalSalidaFFT = senalSalida.calcularFFT();
        respuestaFiltro = filtroActual.obtenerRespuestaFrecuencia(N, fs);
        // Escalar la respuesta del filtro
        respuestaFiltro = respuestaFiltro.multiplicarPorEscalar(senalEntradaFFT.obtenerAmplitudMaxima());
        
        // 3. Obtener los gráficos de tus métodos
        LineChart<Number, Number> chart1 = Graficador.crearGrafica("Señal de Entrada", "Entrada", entradaConRuido, n);
        LineChart<Number, Number> chart2 = Graficador.crearGrafica("FFT de la Señal de Entrada", "FFT Entrada", senalEntradaFFT, 1);
        LineChart<Number, Number> chart3 = Graficador.crearGrafica("Señal de Salida", "Salida", senalSalida, n);
        LineChart<Number, Number> chart4 = Graficador.crearGrafica("FFT de la Señal de Salida", "FFt Salida", senalSalidaFFT, 1);
        XYChart.Series<Number, Number> serieFiltro = Graficador.convertirSignalASerie("Respuesta Filtro", respuestaFiltro, 1);
    
        // Añadir la serie al gráfico existente
        chart2.getData().add(serieFiltro);
        
        // 4. Posicionar en el Grid (Columna, Fila)
        // Fila 0
        SignalGrid.add(chart1, 0, 0); // Arriba Izquierda
        SignalGrid.add(chart2, 1, 0); // Arriba Derecha
        
        // Fila 1
        SignalGrid.add(chart3, 0, 1); // Abajo Izquierda
        SignalGrid.add(chart4, 1, 1); // Abajo Derecha
    }
    
    // Metodos OnAction Señales
    @FXML
    private void SignalApplyOnAction(ActionEvent event) { 
        // Metodo del boton para cambiar la senal de entrada
        
        // Obtener los datos de la interfaz
        String seleccionSenalEntrada = SignalSelector.getValue();
        String freqEntrada = SignalFreq.getText();
        String ampEntrada = SignalAmp.getText();
        
        // Expresión regular para números decimales positivos (ej: 10, 3.14, 0.5)
        String regexDecimalPositivo = "^[0-9]+(\\.[0-9]+)?$";

        // Verificar que ambos inputs cumplan con el formato decimal positivo
        if (!freqEntrada.matches(regexDecimalPositivo) || !ampEntrada.matches(regexDecimalPositivo)) {
            SignalFreq.setText("ERROR: Debe ser decimal positivo");
            SignalAmp.setText("ERROR");
            return;
        }
        
        // Convertir las entradas a double
        double freqEntradaDouble = Double.parseDouble(freqEntrada);
        double ampEntradaDouble = Double.parseDouble(ampEntrada);
        
        // Si la opcion seleccionada es senoidal
        if( seleccionSenalEntrada.equals("Senoidal") ){
            // Ya verificadas las entradas falta actualizar los graficos con los nuevos datos
            senalEntrada = Signal.crearSenoidal(ampEntradaDouble, freqEntradaDouble, fs, N);
        }
        
        // Si la opcion seleccionada es cuadarada
        if( seleccionSenalEntrada.equals("Cuadrada") ){
            // Ya verificadas las entradas falta actualizar los graficos con los nuevos datos
            senalEntrada = Signal.crearCuadrada(ampEntradaDouble, freqEntradaDouble, fs, N);
        }
        
        // Actualizar datos
        actualizarGraficas();
        
    }
    
    @FXML
    private void SignalDeleteOnAction(ActionEvent event) { 
        senalEntrada = Signal.crearSenoidal(0, 0, fs, N);
        
        SignalFreq.setText("0");
        SignalAmp.setText("0");
        SignalSelector.setValue(opcionesTipoSenalEntrada[0]);
        
        actualizarGraficas();
        
    }
    
    
    // Metodos OnAction Filtros
    @FXML
    private void FilterApplyOnAction(ActionEvent event){
        // Metodo del boton para cambiar el filtro
        
        // Obtener datos de la interfaz
        String seleccionFiltro = FilterSelector.getValue();
        String filterFc = FilterFc.getText();
        String filterQ = FilterQ.getText();
        
        // Expresión regular para números decimales positivos (ej: 10, 3.14, 0.5)
        String regexDecimalPositivo = "^[0-9]+(\\.[0-9]+)?$";

        // Verificar que ambos inputs cumplan con el formato decimal positivo
        if (!filterFc.matches(regexDecimalPositivo) || !filterQ.matches(regexDecimalPositivo)) {
            FilterFc.setText("ERROR");
            FilterQ.setText("ERROR");
            return;
        }
        
        // Convertir las entradas a double
        double filterFcDouble = Double.parseDouble(filterFc);
        double filterQDouble = Double.parseDouble(filterQ);
        
        // Verificar opcion seleccionada
        if (seleccionFiltro.equals("Pasa Bajos 1er Orden")){
            // Se actualizo el filtro correspondiente
            filtroActual = GeneradorFiltros.crearPasaBajos(filterFcDouble, fs); 
        }
        if (seleccionFiltro.equals("Pasa Altos 1er Orden")){
            // Se actualizo el filtro correspondiente
            filtroActual = GeneradorFiltros.crearPasaAltos(filterFcDouble, fs); 
        }
        if (seleccionFiltro.equals("Pasa Bajos 2do Orden")){
            // Se actualizo el filtro correspondiente
            filtroActual = GeneradorFiltros.crearPasaBajos2doOrden(filterFcDouble, fs); 
        }
        if (seleccionFiltro.equals("Pasa Altos 2do Orden")){
            // Se actualizo el filtro correspondiente
            filtroActual = GeneradorFiltros.crearPasaAltos2doOrden(filterFcDouble, fs); 
        }
        if (seleccionFiltro.equals("Pasa Banda 2do Orden")){
            // Se actualizo el filtro correspondiente
            filtroActual = GeneradorFiltros.crearPasaBanda(filterFcDouble, filterQDouble, fs); 
        }
        
        // Actualizar los datos
        actualizarGraficas();
        
    }
    
    @FXML
    private void FilterDeleteOnAction(ActionEvent event){
        filtroActual = GeneradorFiltros.crearPasaAltos(0, fs);
        
        // Actualizar el filtro y las graficas correspondientes       
        actualizarGraficas();
    }
    
    @FXML
    private void NoiseSumOnAction(ActionEvent event){
        // Obtener datos de la interfaz
        String seleccionRuido = NoiseSelector.getValue();
        String noiseFreq = NoiseFreq.getText();
        String noiseAmp = NoiseAmp.getText();
        String noisePrctg = NoisePrctg.getText();
        
        // Validar los input
        // Expresión regular para números decimales positivos (ej: 10, 3.14, 0.5)
        String regexDecimalPositivo = "^[0-9]+(\\.[0-9]+)?$";

        // Verificar que ambos inputs cumplan con el formato decimal positivo
        if (!noiseFreq.matches(regexDecimalPositivo) || !noiseAmp.matches(regexDecimalPositivo) || !noisePrctg.matches(regexDecimalPositivo)) {
            NoiseFreq.setText("ERROR");
            NoiseAmp.setText("ERROR");
            NoisePrctg.setText("ERROR");
            return;
        }
        
        // Convertir las entradas a double
        double noiseFreqDouble = Double.parseDouble(noiseFreq);
        double noiseAmpDouble = Double.parseDouble(noiseAmp);
        double noisePrctgDouble = Double.parseDouble(noisePrctg);
        
        // Verificar opcion seleccionada
        if (seleccionRuido.equals("Ruido Aleatorio Plano")){
            // Se actualizo el filtro correspondiente
            ruidoEntrada = ruidoEntrada.sumar(GeneradorRuido.crearRuidoAleatorio(noiseAmpDouble, fs, N));
        }
        if (seleccionRuido.equals("Ruido Aleatorio Porcentual")){
            // Se actualizo el filtro correspondiente
            ruidoEntrada = ruidoEntrada.sumar(GeneradorRuido.aplicarRuidoPorcentual(senalEntrada, noisePrctgDouble));
        }
        if (seleccionRuido.equals("Ruido Interferencia (Senoidal)")){
            // Se actualizo el filtro correspondiente
            ruidoEntrada = ruidoEntrada.sumar(GeneradorRuido.crearRuidoInterferencia(noiseAmpDouble, noiseFreqDouble, fs, N));
        }
        
        // Actualizar datos
        actualizarGraficas();
        
    }
    
    @FXML
    private void NoiseApplyOnAction(ActionEvent event){
        // Obtener datos de la interfaz
        String seleccionRuido = NoiseSelector.getValue();
        String noiseFreq = NoiseFreq.getText();
        String noiseAmp = NoiseAmp.getText();
        String noisePrctg = NoisePrctg.getText();
        
        // Validar los input
        // Expresión regular para números decimales positivos (ej: 10, 3.14, 0.5)
        String regexDecimalPositivo = "^[0-9]+(\\.[0-9]+)?$";

        // Verificar que ambos inputs cumplan con el formato decimal positivo
        if (!noiseFreq.matches(regexDecimalPositivo) || !noiseAmp.matches(regexDecimalPositivo) || !noisePrctg.matches(regexDecimalPositivo)) {
            NoiseFreq.setText("ERROR");
            NoiseAmp.setText("ERROR");
            NoisePrctg.setText("ERROR");
            return;
        }
        
        // Convertir las entradas a double
        double noiseFreqDouble = Double.parseDouble(noiseFreq);
        double noiseAmpDouble = Double.parseDouble(noiseAmp);
        double noisePrctgDouble = Double.parseDouble(noisePrctg);
        
        // Verificar opcion seleccionada
        if (seleccionRuido.equals("Ruido Aleatorio Plano")){
            // Se actualizo el filtro correspondiente
            ruidoEntrada = (GeneradorRuido.crearRuidoAleatorio(noiseAmpDouble, fs, N));
        }
        if (seleccionRuido.equals("Ruido Aleatorio Porcentual")){
            // Se actualizo el filtro correspondiente
            ruidoEntrada = (GeneradorRuido.aplicarRuidoPorcentual(senalEntrada, noisePrctgDouble));
        }
        if (seleccionRuido.equals("Ruido Interferencia (Senoidal)")){
            // Se actualizo el filtro correspondiente
            ruidoEntrada = (GeneradorRuido.crearRuidoInterferencia(noiseAmpDouble, noiseFreqDouble, fs, N));
        }
        
        // Actualizar datos
        actualizarGraficas();
    }
    
    @FXML
    private void NoiseDeleteOnAction(ActionEvent event){
        ruidoEntrada = Signal.crearContinua(0, fs, N);
        
        actualizarGraficas();
    }
}

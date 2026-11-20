package com.example.calculadora;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.MathContext;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvPantalla;

    private String entrada = "0";
    private double acumulado = 0;
    private String operador = null;
    private boolean escribiendoNuevo = true;
    private boolean reemplazar = false;
    private boolean error = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPantalla = findViewById(R.id.tvPantalla);

        final ViewGroup nums = findViewById(R.id.nums);
        registrarBotones(nums);

        nums.post(() -> {
            int lado = findViewById(R.id.btnAC).getWidth();
            if (lado > 0) ajustarAlto(nums, lado);
        });
    }

    private void registrarBotones(ViewGroup grupo) {
        for (int i = 0; i < grupo.getChildCount(); i++) {
            View v = grupo.getChildAt(i);
            if (v instanceof ViewGroup) registrarBotones((ViewGroup) v);
            else if (v instanceof Button) v.setOnClickListener(this);
        }
    }

    private void ajustarAlto(ViewGroup grupo, int lado) {
        for (int i = 0; i < grupo.getChildCount(); i++) {
            View v = grupo.getChildAt(i);
            if (v instanceof ViewGroup) ajustarAlto((ViewGroup) v, lado);
            else if (v instanceof Button) {
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                lp.height = lado;
                v.setLayoutParams(lp);
            }
        }
    }

    @Override
    public void onClick(View v) {
        String t = ((Button) v).getText().toString();

        if (error && !t.equals("AC")) limpiarTodo();

        if (t.matches("[0-9]")) {
            digito(t);
        } else {
            switch (t) {
                case ".": punto(); break;
                case "+": case "-": case "×": case "÷": operacion(t); break;
                case "=": igual(); break;
                case "%": porcentaje(); break;
                case "AC": limpiarTodo(); break;
                case "⌫": borrar(); break;
            }
        }
        tvPantalla.setText(entrada);
    }

    private void digito(String d) {
        if (escribiendoNuevo || reemplazar) {
            entrada = d;
            escribiendoNuevo = false;
            reemplazar = false;
        } else if (entrada.equals("0")) {
            entrada = d;
        } else if (entrada.length() < 12) {
            entrada += d;
        }
    }

    private void punto() {
        if (escribiendoNuevo || reemplazar) {
            entrada = "0.";
            escribiendoNuevo = false;
            reemplazar = false;
        } else if (!entrada.contains(".")) {
            entrada += ".";
        }
    }

    private void operacion(String op) {
        if (operador != null && !escribiendoNuevo) {
            if (!calcular()) return;
            entrada = formatear(acumulado);
        } else if (operador == null) {
            acumulado = Double.parseDouble(entrada);
        }
        operador = op;
        escribiendoNuevo = true;
        reemplazar = false;
    }

    private void igual() {
        if (operador != null && !escribiendoNuevo) {
            if (!calcular()) return;
            entrada = formatear(acumulado);
            operador = null;
            escribiendoNuevo = false;
            reemplazar = true;
        }
    }

    private void porcentaje() {
        double valor = Double.parseDouble(entrada) / 100.0;
        entrada = formatear(valor);
        escribiendoNuevo = false;
        reemplazar = true;
    }

    private void borrar() {
        if (escribiendoNuevo || reemplazar) return;
        if (entrada.length() <= 1 || (entrada.length() == 2 && entrada.startsWith("-"))) {
            entrada = "0";
        } else {
            entrada = entrada.substring(0, entrada.length() - 1);
        }
    }

    private void limpiarTodo() {
        entrada = "0";
        acumulado = 0;
        operador = null;
        escribiendoNuevo = true;
        reemplazar = false;
        error = false;
        tvPantalla.setText(entrada);
    }

    private boolean calcular() {
        double b = Double.parseDouble(entrada);
        double r;
        switch (operador) {
            case "+": r = acumulado + b; break;
            case "-": r = acumulado - b; break;
            case "×": r = acumulado * b; break;
            case "÷":
                if (b == 0) { mostrarError(); return false; }
                r = acumulado / b;
                break;
            default: return true;
        }
        if (Double.isNaN(r) || Double.isInfinite(r)) { mostrarError(); return false; }
        acumulado = r;
        return true;
    }

    private void mostrarError() {
        error = true;
        entrada = "Error";
        operador = null;
        escribiendoNuevo = true;
        reemplazar = false;
    }

    private String formatear(double v) {
        if (v == 0) return "0";
        BigDecimal bd = new BigDecimal(v).round(new MathContext(12)).stripTrailingZeros();
        String s = bd.toPlainString();
        return s.length() > 15 ? bd.toString() : s;
    }
}

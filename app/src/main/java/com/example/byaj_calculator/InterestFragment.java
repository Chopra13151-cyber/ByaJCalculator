package com.example.byaj_calculator;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.Locale;

public class InterestFragment extends Fragment {

    private EditText etPrincipal, etRate, etTime;
    private RadioGroup rgTimeUnit, rgInterestType;
    private TextView tvInterestAmount, tvTotalAmount, tvResultLabel;
    private CardView cardResult;
    private LinearLayout layoutBreakdown;
    private TextView tvBreakdownP, tvBreakdownR, tvBreakdownT, tvBreakdownFormula;
    private Button btnCalculate, btnReset;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interest, container, false);

        etPrincipal = view.findViewById(R.id.etPrincipal);
        etRate = view.findViewById(R.id.etRate);
        etTime = view.findViewById(R.id.etTime);
        rgTimeUnit = view.findViewById(R.id.rgTimeUnit);
        rgInterestType = view.findViewById(R.id.rgInterestType);
        tvInterestAmount = view.findViewById(R.id.tvInterestAmount);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvResultLabel = view.findViewById(R.id.tvResultLabel);
        cardResult = view.findViewById(R.id.cardResult);
        layoutBreakdown = view.findViewById(R.id.layoutBreakdown);
        tvBreakdownP = view.findViewById(R.id.tvBreakdownP);
        tvBreakdownR = view.findViewById(R.id.tvBreakdownR);
        tvBreakdownT = view.findViewById(R.id.tvBreakdownT);
        tvBreakdownFormula = view.findViewById(R.id.tvBreakdownFormula);
        btnCalculate = view.findViewById(R.id.btnCalculate);
        btnReset = view.findViewById(R.id.btnReset);

        btnCalculate.setOnClickListener(v -> calculateInterest());
        btnReset.setOnClickListener(v -> resetFields());

        return view;
    }

    private void calculateInterest() {
        String pStr = etPrincipal.getText().toString().trim();
        String rStr = etRate.getText().toString().trim();
        String tStr = etTime.getText().toString().trim();

        if (pStr.isEmpty() || rStr.isEmpty() || tStr.isEmpty()) {
            // Shake empty fields
            if (pStr.isEmpty()) shakeView(etPrincipal);
            if (rStr.isEmpty()) shakeView(etRate);
            if (tStr.isEmpty()) shakeView(etTime);
            return;
        }

        double principal = Double.parseDouble(pStr);
        double rate = Double.parseDouble(rStr);
        double time = Double.parseDouble(tStr);

        // Convert time to years if months selected
        boolean isMonths = rgTimeUnit.getCheckedRadioButtonId() == R.id.rbMonths;
        double timeInYears = isMonths ? time / 12 : time;
        String timeDisplay = isMonths ? time + " months (" + String.format("%.2f", timeInYears) + " years)" : time + " years";

        boolean isCompound = rgInterestType.getCheckedRadioButtonId() == R.id.rbCompound;

        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        currencyFormat.setMaximumFractionDigits(2);
        currencyFormat.setMinimumFractionDigits(2);

        if (isCompound) {
            // Compound Interest: A = P(1 + r/n)^(nt), n=1 (yearly compounding)
            double amount = principal * Math.pow((1 + rate / 100), timeInYears);
            double interest = amount - principal;

            tvResultLabel.setText("Compound Interest");
            tvInterestAmount.setText("₹ " + currencyFormat.format(interest));
            tvTotalAmount.setText("₹ " + currencyFormat.format(amount));
            tvBreakdownFormula.setText("A = P × (1 + R/100)ᵀ");
            tvBreakdownP.setText("Principal (P) = ₹ " + currencyFormat.format(principal));
            tvBreakdownR.setText("Rate (R) = " + rate + "% per annum");
            tvBreakdownT.setText("Time (T) = " + timeDisplay);
        } else {
            // Simple Interest: SI = PRT/100
            double interest = (principal * rate * timeInYears) / 100;
            double amount = principal + interest;

            tvResultLabel.setText("Simple Interest");
            tvInterestAmount.setText("₹ " + currencyFormat.format(interest));
            tvTotalAmount.setText("₹ " + currencyFormat.format(amount));
            tvBreakdownFormula.setText("SI = (P × R × T) / 100");
            tvBreakdownP.setText("Principal (P) = ₹ " + currencyFormat.format(principal));
            tvBreakdownR.setText("Rate (R) = " + rate + "% per annum");
            tvBreakdownT.setText("Time (T) = " + timeDisplay);
        }

        cardResult.setVisibility(View.VISIBLE);
        layoutBreakdown.setVisibility(View.VISIBLE);

        // Animate result card appearing
        cardResult.setAlpha(0f);
        cardResult.animate().alpha(1f).setDuration(400).start();
    }

    private void resetFields() {
        etPrincipal.setText("");
        etRate.setText("");
        etTime.setText("");
        rgTimeUnit.check(R.id.rbYears);
        rgInterestType.check(R.id.rbSimple);
        cardResult.setVisibility(View.GONE);
        layoutBreakdown.setVisibility(View.GONE);
    }

    private void shakeView(View view) {
        ObjectAnimator shaker = ObjectAnimator.ofFloat(view, "translationX",
                0f, -20f, 20f, -15f, 15f, -10f, 10f, 0f);
        shaker.setDuration(500);
        shaker.start();
    }
}
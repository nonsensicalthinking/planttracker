package com.nonsense.planttracker.android.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.nonsense.planttracker.R;

import java.util.Random;

/**
 * Created by Derek Brooks on 1/28/2018.
 */

public class ColorPicker extends AppCompatActivity {

    private int mColor;

    private int mRed = 0;
    private int mGreen = 0;
    private int mBlue = 0;
    private int mAlpha = 255;

    private SeekBar mAlphaSeekBar;
    private SeekBar mRedSeekBar;
    private SeekBar mGreenSeekBar;
    private SeekBar mBlueSeekBar;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        Intent intent = getIntent();
        mColor = intent.getIntExtra("color", 0);

        bindUi();
    }

    private void bindUi()   {

        mAlphaSeekBar = findViewById(R.id.alphaSeekBar);
        mRedSeekBar = findViewById(R.id.redSeekBar);
        mGreenSeekBar = findViewById(R.id.greenSeekBar);
        mBlueSeekBar = findViewById(R.id.blueSeekBar);

        mAlphaSeekBar.setMax(255);
        mAlphaSeekBar.setProgress(mAlpha, true);
        mRedSeekBar.setMax(255);
        mRedSeekBar.setProgress(mRed, true);
        mGreenSeekBar.setMax(255);
        mGreenSeekBar.setProgress(mGreen, true);
        mBlueSeekBar.setMax(255);
        mBlueSeekBar.setProgress(mBlue, true);

        mRedSeekBar.setOnSeekBarChangeListener(redSeekBarChangeListener);

        mGreenSeekBar.setOnSeekBarChangeListener(greenSeekBarChangeListener);

        mBlueSeekBar.setOnSeekBarChangeListener(blueSeekBarChangeListener);

        mAlphaSeekBar.setOnSeekBarChangeListener(alphaSeekBarChangeListener);

        final EditText hexColor = findViewById(R.id.hexColorEditText);
        hexColor.addTextChangedListener(mColorTextWatcher);

        Button randomButton = findViewById(R.id.randomColorButton);
        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColor = ColorPicker.generateRandomColor();
                updateSliders();
                updateColorPreview();
            }
        });

        Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra("color", mColor);

                setResult(RESULT_OK, i);

                finish();
            }
        });

        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });


        updateSliders();
        updateColorPreview();
    }

    public String getColorHexString()  {
        return ColorPicker.getColorHexString(mRed, mGreen, mBlue, mAlpha);
    }

    public static String getColorHexString(int r, int g, int b, int a)  {
        StringBuilder sb = new StringBuilder("#");

        if (a > 16) {
            sb.append(Integer.toHexString(a));
        }
        else {
            sb.append("0" + Integer.toHexString(a));
        }

        if (r > 16) {
            sb.append(Integer.toHexString(r));
        }
        else {
            sb.append("0" + Integer.toHexString(r));
        }

        if (g > 16) {
            sb.append(Integer.toHexString(g));
        }
        else {
            sb.append("0" + Integer.toHexString(g));
        }

        if (b > 16) {
            sb.append(Integer.toHexString(b));
        }
        else {
            sb.append("0" + Integer.toHexString(b));
        }

        return sb.toString();
    }


    private void updateColorPreview()   {
        String hexColor = getColorHexString();

        try {
            ImageView colorPreviewImageView = findViewById(R.id.colorPickerImageView);
            ColorDrawable gd = (ColorDrawable)colorPreviewImageView.getDrawable();
            int parsedColor = Color.parseColor(hexColor);

            gd.setColor(Color.parseColor(hexColor));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setColorPreviewText()   {
        String colorHex = getColorHexString();

        EditText hexColorEditText = findViewById(R.id.hexColorEditText);
        hexColorEditText.removeTextChangedListener(mColorTextWatcher);
        hexColorEditText.setText(colorHex);
        hexColorEditText.addTextChangedListener(mColorTextWatcher);
    }

    private final SeekBar.OnSeekBarChangeListener redSeekBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            mRed = i;

            setColorPreviewText();
            updateColorPreview();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private final SeekBar.OnSeekBarChangeListener greenSeekBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    mGreen = i;

                    setColorPreviewText();
                    updateColorPreview();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };

    private final SeekBar.OnSeekBarChangeListener blueSeekBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    mBlue = i;
                    setColorPreviewText();
                    updateColorPreview();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };

    private final SeekBar.OnSeekBarChangeListener alphaSeekBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    mAlpha = i;
                    setColorPreviewText();
                    updateColorPreview();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };

    private final TextWatcher mColorTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            try {
                final EditText hexColor = findViewById(R.id.hexColorEditText);

                if (!hexColor.getText().toString().equals("") &&
                        (hexColor.length() == 7 || hexColor.length() == 9)) {
                    String colorHex = hexColor.getText().toString();
                    mColor = Color.parseColor(colorHex);
                    updateColorPreview();
                    updateSliders();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void updateSliders()    {
//        mAlphaSeekBar.setOnSeekBarChangeListener(null);
//        mRedSeekBar.setOnSeekBarChangeListener(null);
//        mGreenSeekBar.setOnSeekBarChangeListener(null);
//        mBlueSeekBar.setOnSeekBarChangeListener(null);

        mAlpha = (mColor >> 24) & 0xff; // or color >>> 24
        mRed = (mColor >> 16) & 0xff;
        mGreen = (mColor >>  8) & 0xff;
        mBlue = (mColor) & 0xff;

        mAlphaSeekBar.setProgress(mAlpha, true);
        mRedSeekBar.setProgress(mRed, true);
        mGreenSeekBar.setProgress(mGreen, true);
        mBlueSeekBar.setProgress(mBlue, true);
    }

    public static String generateRandomHexColor()   {
        Random r = new Random();
        r.setSeed(System.currentTimeMillis());

        int red = r.nextInt(255);
        int green = r.nextInt(255);
        int blue = r.nextInt(255);

        return ColorPicker.getColorHexString(red, green, blue, 255);
    }

    public static int generateRandomColor() {
        return Color.parseColor(generateRandomHexColor());
    }
}

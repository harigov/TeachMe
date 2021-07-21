package com.knuron.teachme;
	
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Sample code that invokes the speech recognition intent API.
 */
public class TeachMeActivity extends Activity {

    private static final String TAG = "VoiceRecognition";

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    
    private TextToSpeech mTts;
    
    private EditText spokenText;
    private EditText outputText;
    private Button speakButton;
    private Button explainButton;

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        spokenText = (EditText) findViewById(R.id.input_text);
        outputText = (EditText)findViewById(R.id.output_text);
        
        spokenText.setMovementMethod(new ScrollingMovementMethod());
        outputText.setMovementMethod(new ScrollingMovementMethod());

        speakButton = (Button) findViewById(R.id.ask_button);
        // Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            speakButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	startVoiceRecognitionActivity();
                }
            });
        } else {
            speakButton.setEnabled(false);
            speakButton.setText("Recognizer not present");
        }
        
        explainButton = (Button) findViewById(R.id.explain_button);
        explainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	EditText text = (EditText)findViewById(R.id.input_text);
            	if (text.getText().length() > 0) {
            		solve(text.getText().toString());
            	} else {
            		speak ("Uh oh, don't have anything to ask?");
            	}
            }
        });
        
        mTts = new TextToSpeech(this,
                new TextToSpeech.OnInitListener() {
        	public void onInit(int status) {
                // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
                if (status == TextToSpeech.SUCCESS) {
                    // Set preferred language to US english.
                    // Note that a language may not be available, and the result will indicate this.
                    int result = mTts.setLanguage(Locale.US);
                    // Try this someday for some interesting results.
                    // int result mTts.setLanguage(Locale.FRANCE);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                       // Lanuage data is missing or the language is not supported.
                        Log.e(TAG, "Language is not available.");
                    } else {
                    }
                } else {
                    // Initialization failed.
                    Log.e(TAG, "Could not initialize TextToSpeech.");
                }
        	}
        }
        );
    }

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");

        // Given an hint to the recognizer about what the user is going to say
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Specify how many results you want to receive. The results will be sorted
        // where the first result is the one with higher confidence.
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            if (matches.size() > 0) {
	            spokenText.setText(matches.get(0));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void type(String text) {
    	outputText.append(text + "\n");
    }
    
    private void speak (String text) {
		mTts.playSilence(200, TextToSpeech.QUEUE_ADD, null);
		mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
    }
    
    private void solve (String input) {
    	spokenText.setEnabled(false);
    	speakButton.setEnabled(false);
    	explainButton.setEnabled(false);
    	
		try {
			EquationGenerator eqGen = new EquationGenerator();
			speak ("I got the input");
			type (input);
			speak ("Let me think...");

			ArrayList<Equation> simplifiedEqns = eqGen.generate(input);
			eqGen.printVariables();
			
			EquationSolver solver = new EquationSolver();
			HashMap<String,Double> bindings = new HashMap<String,Double>();
			solver.solve(simplifiedEqns, bindings);
			
			speak ("OK, got it");
			speak ("Now let's go over what we know");
			
			// speak all the equations with single unknown
			for (int i = 0; i < simplifiedEqns.size(); ++i) {
				if (simplifiedEqns.get(i).numUnknowns() == 1) {
					speak(simplifiedEqns.get(i).getSpokenEquation());
					type(simplifiedEqns.get(i).getEquation());
				}
			}
			
			speak ("We are given more information about other variables with relations");
			
			for (int i = 0; i < simplifiedEqns.size(); ++i) {
				if (simplifiedEqns.get(i).numUnknowns() > 1) {
					speak(simplifiedEqns.get(i).getSpokenEquation());
					type(simplifiedEqns.get(i).getEquation());
				}
			}
			
			speak ("Using those relations, we can solve the problem and the result is");
			
			type ("The solution is");
			
			for(String bind: bindings.keySet()) {
				type (eqGen.getActualName(bind) + " " + bindings.get(bind));
				speak (eqGen.getActualName(bind) + " " + bindings.get(bind));
			}
		} catch (Exception e) {
			System.out.println (e.getMessage());
			speak ("Oops that stumped me. Do you want me to look online and see if anyone has better answer?");
		}
		
		spokenText.setEnabled(true);
		speakButton.setEnabled(true);
		explainButton.setEnabled(true);
    }
}

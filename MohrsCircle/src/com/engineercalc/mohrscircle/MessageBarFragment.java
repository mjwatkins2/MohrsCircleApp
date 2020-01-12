package com.engineercalc.mohrscircle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class MessageBarFragment extends SherlockFragment {

	private MohrsCircleActivity mActivity;
	private boolean hasExplainDialog = false;
	private boolean isPlayStoreLauncher = false;
	private int messageStringId = 0, explainDialogStringId = 0;
	private int hideDelay = 1;
	private Handler h =  new Handler();
	private Runnable timer = new Runnable() {
		@Override
		public void run() {
			removeFragment();
		}
	};
	
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
        
    	setRetainInstance(true);
    	
        try {
            mActivity = (MohrsCircleActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " is not a MohrsCircleActivity.");
        }
    }
    
    public MessageBarFragment setOptions(int messageId) {
    	hasExplainDialog = false;
    	messageStringId = messageId;
    	return this;
    }
    
    public MessageBarFragment setOptions(int messageId, int explainDialogMessageId) {
    	hasExplainDialog = true;
    	messageStringId = messageId;
    	explainDialogStringId = explainDialogMessageId;
    	return this;
    }
    
    public MessageBarFragment setOptions(int messageId, boolean isPlayStoreUpgradeLink) {
    	hasExplainDialog = false;
    	messageStringId = messageId;
    	isPlayStoreLauncher = isPlayStoreUpgradeLink;
    	return this;
	}	
    
    public MessageBarFragment setHideDelay(int hideDelayMillis) {
    	hideDelay = hideDelayMillis;
    	return this;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
    	
    	View view =  inflater.inflate(R.layout.messagebar_fragment, container, false);

    	Button buttonHideBar = (Button) view.findViewById(R.id.messagebar_hide);
    	
		buttonHideBar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				removeFragment();
			}
		});
		
    	Button buttonExplain = (Button) view.findViewById(R.id.messagebar_show);
		
    	if (hasExplainDialog) {
    		buttonExplain.setVisibility(View.VISIBLE);
    		
    		buttonExplain.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				h.removeCallbacks(timer);
    				MessageDialog message = new MessageDialog();
    				Bundle args = new Bundle();
    				args.putInt("title", messageStringId);
    				args.putInt("message", explainDialogStringId);
    				message.setArguments(args);
    				message.show(mActivity.getSupportFragmentManager(), "message");
    			}
    		});
    	} else if (isPlayStoreLauncher) {
    		buttonExplain.setVisibility(View.VISIBLE);
    		
    		buttonExplain.setText(R.string.Upgrade);
    		
    		buttonExplain.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				h.removeCallbacks(timer);
    				final String appName = "com.engineercalc.mohrscircle";
    				try {
    				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appName)));
    				} catch (android.content.ActivityNotFoundException anfe) {
    				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="+appName)));
    				}
    			}
    		});
    	} else {
    		buttonExplain.setVisibility(View.GONE);
    	}

    	TextView msg = (TextView) view.findViewById(R.id.messagebar_message);
    	if (messageStringId > 0) {
    		msg.setText(messageStringId);
    	} else {
    		msg.setText("");
    	}
		
		h.postDelayed(timer, hideDelay);

    	return view;
    }
    
    @Override
    public void onDestroyView() {
    	h.removeCallbacks(timer);
    	super.onDestroyView();
    }
    
    private void removeFragment() {
		h.removeCallbacks(timer);
		// Careful on screen rotations here
		mActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
    
    private void setDontShowMessageAgain(boolean dontShowAgain) {
    	mActivity.setSuppressMessageBar(messageStringId, dontShowAgain);
    }

    @SuppressLint("ValidFragment")
	public class MessageDialog extends DialogFragment {
    	
        public MessageDialog() {
        	
        }
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
        	
        	setRetainInstance(true);
        	
        	Bundle args = getArguments();
        	
        	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        	
        	CheckBox checkbox = new CheckBox(getActivity());
        	checkbox.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        	checkbox.setText(R.string.DontShowAgain);
        	checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					setDontShowMessageAgain(isChecked);
				}
        	});
        	if (args != null) {
	        	if (args.getInt("title") > 0) {
	        		builder.setTitle(args.getInt("title"));
	        	}
	        	if (args.getInt("message") > 0) {
	        		builder.setMessage(args.getInt("message"));
	        	}
        	}
        	builder.setView(checkbox)
        		   .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							removeFragment();
						}
					});
        	
        	AlertDialog dialog = builder.create();
        	dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					removeFragment();
				}
        	});
        	dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					removeFragment();
				}
        	});
        	return dialog;
        }
        
        /**
         * This is added due to a crash on configuration change, and to prevent dialog from being removed:
         * http://stackoverflow.com/questions/11160412/why-use-fragmentsetretaininstanceboolean
         */
        @Override
        public void onDestroyView() {
          if (getDialog() != null && getRetainInstance())
            getDialog().setOnDismissListener(null);
          super.onDestroyView();
        }
    }
}

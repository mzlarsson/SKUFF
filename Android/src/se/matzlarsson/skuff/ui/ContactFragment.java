package se.matzlarsson.skuff.ui;

import se.matzlarsson.skuff.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
public class ContactFragment extends Fragment implements Refreshable {
     
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.fragment_contact, container, false);
    }

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}
}
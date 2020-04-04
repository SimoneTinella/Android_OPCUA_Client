package OpcUtils;


import androidx.fragment.app.FragmentManager;

public class BackListener implements FragmentManager.OnBackStackChangedListener {

    private int prec_size;
    private FragmentManager fm;

    public BackListener(FragmentManager fm) {
        this.fm = fm;
        prec_size = 0;
    }

    @Override
    public void onBackStackChanged() {
        if (fm.getBackStackEntryCount() < prec_size)
            ManagerOPC.getIstance().pop();
        prec_size = fm.getBackStackEntryCount();
    }
}

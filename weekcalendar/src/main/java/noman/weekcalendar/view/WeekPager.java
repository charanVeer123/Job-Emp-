package noman.weekcalendar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import noman.weekcalendar.R;
import noman.weekcalendar.adapter.PagerAdapter;
import noman.weekcalendar.eventbus.BusProvider;
import noman.weekcalendar.eventbus.Event;
import noman.weekcalendar.fragment.WeekFragment;

/**
 * Created by nor on 12/5/2015.
 */
public class WeekPager extends ViewPager {

    private static final String TAG = "WeekCalendar";
    private PagerAdapter adapter;
    private int pos;
    private boolean check;
    public static int NUM_OF_PAGES;
    private TypedArray typedArray;

    public WeekPager(Context context) {
        super(context);
        initialize(null);
    }

    public WeekPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    private void initialize(AttributeSet attrs) {
        if (attrs != null) {
            typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WeekCalendar);
            NUM_OF_PAGES = typedArray.getInt(R.styleable.WeekCalendar_numOfPages, 100);
        }
        setId(idCheck());
        initPager(new DateTime());
        BusProvider.getInstance().register(this);

    }

    private void initPager(DateTime dateTime) {
        pos = NUM_OF_PAGES / 2;
        adapter = new PagerAdapter(getContext(), ((AppCompatActivity) getContext())
                .getSupportFragmentManager(), dateTime, typedArray);
        setAdapter(adapter);
        addOnPageChangeListener(new ViewPager
                .SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (!check)
                    if (position < pos)
                        adapter.swipeBack();
                    else if (position > pos)
                        adapter.swipeForward();
                pos = position;
                check = false;

            }

        });
        setOverScrollMode(OVER_SCROLL_NEVER);
        setCurrentItem(pos);
        if (typedArray != null)
            setBackgroundColor(typedArray.getColor(R.styleable.WeekCalendar_daysBackgroundColor,
                    ContextCompat.getColor(getContext(), R.color.colorPrimary)));
        if (WeekFragment.selectedDateTime == null)
            WeekFragment.selectedDateTime = new DateTime();
    }

    @Subscribe
    public void setCurrentPage(Event.SetCurrentPageEvent event) {
        Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
        check = true;
        if (event.getDirection() == 1)
            adapter.swipeForward();
        else
            adapter.swipeBack();
        setCurrentItem(getCurrentItem() + event.getDirection());

    }

    @Subscribe
    public void reset(Event.ResetEvent event) {
        WeekFragment.selectedDateTime = new DateTime(WeekFragment.CalendarStartDate);
        //WeekFragment.CalendarStartDate = new DateTime();
        initPager(WeekFragment.CalendarStartDate);
    }

    @Subscribe
    public void setSelectedDate(Event.SetSelectedDateEvent event) {
        WeekFragment.selectedDateTime = event.getSelectedDate();
        initPager(event.getSelectedDate());
    }

    @Subscribe
    public void setStartDate(Event.SetStartDateEvent event) {
        WeekFragment.CalendarStartDate = event.getStartDate();
        WeekFragment.selectedDateTime = event.getStartDate();
        initPager(event.getStartDate());
    }


    private int idCheck() {

        int id = 0;
        while (findViewById(++id) != null) ;
        return id;
    }
}

package no.rustelefonen.hap.main.tabs.program;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.customviews.NestedScrollView;
import no.rustelefonen.hap.customviews.graph.Graph;
import no.rustelefonen.hap.customviews.graph.GraphData;
import no.rustelefonen.hap.customviews.graph.GraphUserSpot;
import no.rustelefonen.hap.entities.Info;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.main.tabs.activity.MainActivity;
import no.rustelefonen.hap.main.tabs.activity.MainTabPageHandler;
import no.rustelefonen.hap.main.tabs.info.InfoDetailActivity;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.persistence.dao.InfoDao;
import no.rustelefonen.hap.tabs.misc.TabPage;
import no.rustelefonen.hap.util.AnimationListenerAdapter;
import no.rustelefonen.hap.util.ViewCallback;

import static no.rustelefonen.hap.util.DialogHelper.showInfoDialog;
import static no.rustelefonen.hap.util.Dimensions.toDP;

/**
 * Created by martinnikolaisorlie on 26/01/16.
 */
public class ProgramTab extends TabPage {

    @BindView(R.id.overlay) FrameLayout overlay;
    @BindView(R.id.nested_scroll_view) NestedScrollView scrollView;
    @BindView(R.id.graph_view) Graph graph;
    @BindView(R.id.resisted_piechart) PieChart resistedChartPie;
    @BindView(R.id.resisted_no_data) TextView resistedNoData;
    @BindView(R.id.smoked_piechart) PieChart smokedChartPie;
    @BindView(R.id.smoked_no_data) TextView smokedNoData;
    @BindView(R.id.tutorial_arrow) ImageView tutorialArrow;

    private Unbinder unbinder;
    private GraphUserSpot userSpot;
    private Handler handler;
    private Runnable graphUpdater;
    private MainActivity mainActivity;
    private List<Graph.Section> sections;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.program_tab, container, false);
        unbinder = ButterKnife.bind(this, v);
        EventBus.getDefault().register(this);
        handler = new Handler();
        mainActivity = (MainActivity) getActivity();
        initNoDataTextViews();
        userSpot = new GraphUserSpot(getContext());
        userSpot.setLineColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        userSpot.setDotColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        userSpot.setLineWidth(toDP(2));
        graph.addUserSpot(userSpot);

        GraphData thc = new GraphData();
        GraphData feelings = new GraphData();

        thc.setData(Arrays.asList(100f, 97f, 90f, 75f, 50f, 45f, 40f, 25f, 20f, 15f, 10f, 0f));
        feelings.setData(Arrays.asList(15f, 25f, 30f, 50f, 80f, 75f, 73f, 50f, 25f, 15f, 13f, 7f, 0f));

        thc.getLinePaint().setColor(Color.parseColor("#A0D988"));
        thc.setGradientFillTopColor(Color.parseColor("#CC90C978"));
        thc.setGradientFillBottomColor(Color.parseColor("#1990C978"));

        feelings.getLinePaint().setColor(Color.parseColor("#92D6ED"));
        feelings.setGradientFillTopColor(Color.parseColor("#CC82C6DD"));
        feelings.setGradientFillBottomColor(Color.parseColor("#1982C6DD"));

        graph.addGraphData(thc);
        graph.addGraphData(feelings);

        List<Graph.AxisDetails> xAxisDetails = Arrays.asList(
                new Graph.AxisDetails("Uke:", 0, false),
                new Graph.AxisDetails("1", 0.1666f),
                new Graph.AxisDetails("2", 0.3333f),
                new Graph.AxisDetails("3", 0.5f),
                new Graph.AxisDetails("4", 0.6666f),
                new Graph.AxisDetails("5", 0.8333f),
                new Graph.AxisDetails("6", 1)
        );

        List<Graph.AxisDetails> yAxisDetails = Arrays.asList(
                new Graph.AxisDetails("Høy", 0),
                new Graph.AxisDetails("Lav", 1)
        );

        sections = Arrays.asList(
                new Graph.Section("Fase 1", 0.262f),
                new Graph.Section("Fase 2", 0.5f),
                new Graph.Section("Fase 3", 1f)
        );

        graph.setxAxisDetails(xAxisDetails);
        graph.setyAxisDetails(yAxisDetails);
        graph.setSections(sections);

        graph.setSectionListener(new Graph.OnSelectSectionListener() {
            @Override public void onSelect(int sectionIndex) {
                InfoDao infoDao = new InfoDao(mainActivity);
                Info info = infoDao.getInfoByTitle(sections.get(sectionIndex).label+"%");
                if(info != null){
                    Intent intent = new Intent(getContext(), InfoDetailActivity.class);
                    intent.putExtra(InfoDetailActivity.HELP_INFO_ID_EXTRA, info.getId());
                    startActivity(intent);
                }
            }
        });
        overlay.measure(0, 0);

        return v;
    }

    @OnClick({R.id.your_pos_txt, R.id.your_pos_img, R.id.graph_view})
    public void bounceUserSpot() {
        TranslateAnimation animateUp = new TranslateAnimation(0, 0, 0, -toDP(20));
        final TranslateAnimation bounce = new TranslateAnimation(0, 0, -toDP(20), 0);
        animateUp.setDuration(200);
        bounce.setDuration(200);
        bounce.setInterpolator(new BounceInterpolator());
        animateUp.setAnimationListener(new AnimationListenerAdapter() {
            public void onAnimationEnd(Animation animation) {
                userSpot.startAnimation(bounce);
            }
        });

        userSpot.startAnimation(animateUp);
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewCallback.onAppear(graph, new Runnable() {
            @Override
            public void run() {
                final int interval = 5000;
                graphUpdater = new Runnable() {
                    public void run() {
                        updateGraph();
                        handler.postDelayed(this, interval);
                    }
                };
                handler.post(graphUpdater);
            }
        });

        User user = mainActivity.getUser();

        new AsyncTriggerPieBuilder(resistedChartPie, resistedNoData, user.getResistedTriggers()).execute();
        new AsyncTriggerPieBuilder(smokedChartPie, smokedNoData, user.getSmokedTriggers()).execute();
    }

    private void updateGraph() {
        User user = mainActivity.getUser();
        if (user == null || graph == null) return;

        float programDuration = 86400 * 42; // 42 days
        graph.animateUserSpotTo(userSpot, user.secondsSinceStarted() / programDuration);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(graphUpdater);
        despawnOverlay();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
    }

    private void initNoDataTextViews() {
        int txtColor = Color.parseColor("#AAAAAA");
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("Trykk _ for å legge til data");
        builder.setSpan(new ForegroundColorSpan(txtColor), 0, builder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        smokedNoData.measure(0, 0);

        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_border_color_black_24dp).mutate(); //mutate is important
        drawable.setColorFilter(txtColor, PorterDuff.Mode.SRC_ATOP);
        drawable.setBounds(0, 0, smokedNoData.getMeasuredHeight(), smokedNoData.getMeasuredHeight());
        builder.setSpan(new ImageSpan(drawable), 6, 7, 0);

        smokedNoData.setText(builder);
        resistedNoData.setText(builder);
    }

    @OnClick({R.id.smoked_info, R.id.avoided_info, R.id.graph_info})
    public void showDialog(ImageView imageView) {
        switch (imageView.getId()) {
            case R.id.graph_info:
                //showInfoDialog(getActivity(), "Abstinensoversikt", "Graf THC: Denne grafen viser tiden det kan ta for THC-metabolitter å skilles ut av kroppen om du har brukt cannabis regelmessig. Det kan ta opptil seks uker (og i noen tilfeller lengre) før THC-metabolitter er skilt ut av kroppen, dersom du har brukt cannabis over tid.\n\nGraf humørsvingninger: Denne grafen viser hva du kan forvente av humørsvingninger når du slutter med cannabis etter jevnlig bruk.  Grafen er en generell fremstilling, men det vil være individuelle forskjeller i graden av humørsvingninger man vil oppleve når man slutter.\n\nDen røde nålen viser hvor du befinner deg i programmet. Klikk på faseoverskriftene for å lese mer.\n\nGrafene er kun en visuell illustrasjon for at du lettere vil kunne få en oversikt over vanlige utfordringer og abstinenser i en sluttprosess, og for å vise at THC-metabolitter (nedbrytningsprodukter) skilles ut av kroppen over tid. Det er ikke nødvendigvis noe forhold mellom THC grafen og humørsvingninger. Modellen er basert på kliniske erfaringer og empiri. Det vil si brukeres erfaringer med å slutte med cannabis. Dette er ikke en modell som forteller hvordan slutteprosessen fungerer nevrobiologisk.   Modellen er under revidering, og det kan komme endringer og justeringer i denne modellen over tid, når ytterligere kliniske erfaringer vil bli systematisert sammen med forskningen på feltet.");
                InfoDao infoDao = new InfoDao((OrmLiteActivity) getActivity());
                Info graphInfo = infoDao.getInfoByTitle("THC-metabolitter");
                Intent intent = new Intent(getContext(), InfoDetailActivity.class);
                intent.putExtra(InfoDetailActivity.HELP_INFO_ID_EXTRA, graphInfo.getId());
                startActivity(intent);
                break;
            case R.id.avoided_info:
                showInfoDialog(getActivity(), "Positive triggere", "Dersom du motstår å ruse deg kan du registrere det i triggerdagboken.\n\nOver tid vil dette vinduet gi deg en god oversikt over hva som hjelper best, når suget melder seg.");
                break;
            case R.id.smoked_info:
                showInfoDialog(getActivity(), "Negative triggere", "Dersom du ruser deg kan du registrere det i triggerdagboken.\n\nOver tid vil dette vinduet gi deg en god oversikt over hvilke situasjoner du bør passe deg for.");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTabSelected(MainTabPageHandler.TabSelectedEvent tabSelectedEvent) {
        if (tabSelectedEvent.tabClass != this.getClass()) return;

        ViewCallback.onAppear(graph, new Runnable() {
            @Override
            public void run() {
                bounceUserSpot();
            }
        });
    }

    @OnClick({R.id.smoked_chartpie_card, R.id.resisted_chartpie_card})
    public void showOverlay(CardView cardView) {
        if(cardView.getId() == R.id.smoked_chartpie_card && smokedChartPie.getVisibility() == View.VISIBLE) return;
        if(cardView.getId() == R.id.resisted_chartpie_card && resistedChartPie.getVisibility() == View.VISIBLE) return;
        if (overlay.getVisibility() == View.VISIBLE) return;

        overlay(true);
    }

    @OnTouch(R.id.overlay)
    public boolean despawnOverlay() {
        if (overlay.getVisibility() == View.GONE) return false;
        overlay(false);
        return true;
    }

    private void overlay(boolean doSpawn) {
        int scrolledDist = scrollView.getScrollY() - mainActivity.getAppBarScrolledOffset();
        int offsetFromBottom = (int) (toDP(110) + tutorialArrow.getMeasuredHeight());

        tutorialArrow.setY(scrollView.getHeight() + scrolledDist - offsetFromBottom);
        scrollView.setEnableScrolling(!doSpawn);
        overlay.setVisibility(doSpawn ? View.VISIBLE : View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void scrollToBottom(TriggerAddedEvent event) {
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                mainActivity.onNestedFling(0, 10000);
                scrollView.scrollTo(0, scrollView.getBottom());
            }
        }, 300);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void resetTriggers(TriggerResetEvent event){
        User user = mainActivity.getUser();
        new AsyncTriggerPieBuilder(resistedChartPie, resistedNoData, user.getResistedTriggers()).execute();
        new AsyncTriggerPieBuilder(smokedChartPie, smokedNoData, user.getSmokedTriggers()).execute();
    }

    public static class TriggerAddedEvent {}
    public static class TriggerResetEvent{}
}
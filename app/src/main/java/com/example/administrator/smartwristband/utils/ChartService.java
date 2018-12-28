package com.example.administrator.smartwristband.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.List;

public class ChartService {
    private static ChartService mInstance = null;
    private GraphicalView mGraphicalView;
    private XYMultipleSeriesDataset multipleSeriesDataset = new XYMultipleSeriesDataset();// 数据集容器
    private XYMultipleSeriesRenderer multipleSeriesRenderer = new XYMultipleSeriesRenderer();// 渲染器容器
    private XYSeries mSeries = new TimeSeries("Heart Rate");// 单条曲线数据集
    private XYSeriesRenderer mRenderer;// 单条曲线渲染器
    private Context context;

    public ChartService(Context context) {
        this.context = context;
        multipleSeriesDataset.addSeries(mSeries);
        mRenderer = new XYSeriesRenderer();
        mRenderer.setColor(Color.RED);
        mRenderer.setPointStyle(PointStyle.SQUARE);
        mRenderer.setFillPoints(true);
        XYMultipleSeriesRenderer localXYMultipleSeriesRenderer = multipleSeriesRenderer;
        localXYMultipleSeriesRenderer.setBackgroundColor(0);
        localXYMultipleSeriesRenderer.setMargins(new int[]{50, 65, 40, 5});
        localXYMultipleSeriesRenderer.setMarginsColor(Color.argb(0, 1, 1, 1));
        localXYMultipleSeriesRenderer.setAxesColor(Color.WHITE);
        localXYMultipleSeriesRenderer.setAxisTitleTextSize(24.0F);
        localXYMultipleSeriesRenderer.setShowGrid(true);
        localXYMultipleSeriesRenderer.setPointSize(3.0F);
        localXYMultipleSeriesRenderer.setGridColor(Color.BLACK);
        localXYMultipleSeriesRenderer.setLabelsColor(Color.BLACK);
        localXYMultipleSeriesRenderer.setYLabelsColor(0, Color.BLACK);
        localXYMultipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        localXYMultipleSeriesRenderer.setYLabelsPadding(4.0F);
        localXYMultipleSeriesRenderer.setXLabelsColor(Color.BLACK);
        localXYMultipleSeriesRenderer.setLabelsTextSize(20.0F);
        localXYMultipleSeriesRenderer.setLegendTextSize(20.0F);
        localXYMultipleSeriesRenderer.setPanEnabled(false, false);
        localXYMultipleSeriesRenderer.setZoomEnabled(false, false);
        localXYMultipleSeriesRenderer.setXTitle("          Time(秒)");
        localXYMultipleSeriesRenderer.setYTitle("               BPM");
        localXYMultipleSeriesRenderer.addSeriesRenderer(mRenderer);


    }


    public static ChartService getGraphView(Context context) {
        if (mInstance == null) {
            mInstance = new ChartService(context);
        }
        ChartService localGraphView = mInstance;
        return localGraphView;
    }

    ///获取图表
    public GraphicalView getGraphicalView() {

        mGraphicalView = ChartFactory.getCubeLineChartView(context,
                multipleSeriesDataset, multipleSeriesRenderer, 0.1f);
        return mGraphicalView;
    }


    /**
     * 获取渲染器
     *
     * @param maxX       x轴最大值
     * @param maxY       y轴最大值
     * @param chartTitle 曲线的标题
     * @param xTitle     x轴标题
     * @param yTitle     y轴标题
     * @param axeColor   坐标轴颜色
     * @param labelColor 标题颜色
     * @param curveColor 曲线颜色
     * @param gridColor  网格颜色
     */
    public void setXYMultipleSeriesRenderer(double maxX, double maxY,
                                            String chartTitle, String xTitle, String yTitle, int axeColor,
                                            int labelColor, int curveColor, int gridColor) {
        multipleSeriesRenderer = new XYMultipleSeriesRenderer();
        if (chartTitle != null) {
            multipleSeriesRenderer.setChartTitle(chartTitle);
        }
        multipleSeriesRenderer.setXTitle("  Time(秒)");
        multipleSeriesRenderer.setYTitle("     BMP");
        multipleSeriesRenderer.setRange(new double[]{0, maxX, 0, maxY});//xy轴的范围
        multipleSeriesRenderer.setLabelsColor(labelColor);
        multipleSeriesRenderer.setXLabels(10);
        multipleSeriesRenderer.setYLabels(10);
        multipleSeriesRenderer.setXLabelsAlign(Paint.Align.RIGHT);
        multipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        multipleSeriesRenderer.setAxisTitleTextSize(20);
        multipleSeriesRenderer.setChartTitleTextSize(20);
        multipleSeriesRenderer.setLabelsTextSize(20);
        multipleSeriesRenderer.setLegendTextSize(20);
        multipleSeriesRenderer.setPointSize(2f);//曲线描点尺寸
        multipleSeriesRenderer.setFitLegend(true);
        multipleSeriesRenderer.setMargins(new int[]{20, 30, 15, 20});
        multipleSeriesRenderer.setShowGrid(true);
        multipleSeriesRenderer.setZoomEnabled(true, false);
        multipleSeriesRenderer.setAxesColor(axeColor);
        multipleSeriesRenderer.setGridColor(gridColor);
        multipleSeriesRenderer.setBackgroundColor(Color.WHITE);//背景色
        multipleSeriesRenderer.setMarginsColor(Color.WHITE);//边距背景色，默认背景色为黑色，这里修改为白色
        mRenderer = new XYSeriesRenderer();
        mRenderer.setColor(curveColor);
        mRenderer.setPointStyle(PointStyle.CIRCLE);//描点风格，可以为圆点，方形点等等
        multipleSeriesRenderer.addSeriesRenderer(mRenderer);
    }

    /**
     * 根据新加的数据，更新曲线，只能运行在主线程
     *
     * @param x 新加点的x坐标
     * @param y 新加点的y坐标
     */
    public void updateChart(double x, double y) {
        mSeries.add(x, y);
        mGraphicalView.repaint();//此处也可以调用invalidate()
    }

    /**
     * 添加新的数据，多组，更新曲线，只能运行在主线程
     *
     * @param xList
     * @param yList
     */
    public void updateChart(List<Double> xList, List<Double> yList) {
        for (int i = 0; i < xList.size(); i++) {
            mSeries.add(xList.get(i), yList.get(i));
        }
        mGraphicalView.repaint();//此处也可以调用invalidate()
    }

    public void clearGraph() {
        this.mSeries.clear();
    }
}

package app.com.shoppingcar;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends Activity {

    @BindView(R.id.third_recyclerview)
    RecyclerView thirdRecyclerview;
    @BindView(R.id.third_allselect)
    TextView thirdAllselect;
    @BindView(R.id.third_totalprice)
    TextView thirdTotalprice;
    @BindView(R.id.third_totalnum)
    TextView thirdTotalnum;
    @BindView(R.id.third_submit)
    TextView thirdSubmit;
    @BindView(R.id.third_pay_linear)
    LinearLayout thirdPayLinear;
    //模拟网络
    //private List<ShopBean.OrderDataBean.CartlistBean> mAllOrderList = new ArrayList<>();
    //
    private List<ShopDataBean.DataBean.ListBean> mAllOrderList = new ArrayList<>();
    private ShopAdapter adapter;
    private LinearLayoutManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        getData();

        // 1 为选中  2 选中
        thirdAllselect.setTag(1);

        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        adapter = new ShopAdapter(this);

        thirdRecyclerview.setLayoutManager(manager);
        thirdRecyclerview.setAdapter(adapter);

        //本地数据可直接添加数据
        //网络请求使用(异步),直接添加数据为空,异步获取数据源后再添加数据
//        adapter.add(mAllOrderList);


        adapter.setCheckBoxListener(new ShopAdapter.CheckBoxListener() {
            @Override
            public void check(int position, int count, boolean check, List<ShopDataBean.DataBean.ListBean> list) {
                sum(list);
            }
        });

        adapter.setCustomViewListener(new ShopAdapter.CustomViewListener() {
            @Override
            public void click(int count, List<ShopDataBean.DataBean.ListBean> list) {
                sum(list);
            }
        });

        adapter.setDelListener(new ShopAdapter.DelListener() {
            @Override
            public void del(int position, List<ShopDataBean.DataBean.ListBean> list) {
                sum(list);
            }
        });
    }

    float price = 0;
    int count;

    /**
     * 计算总价
     *
     * @param mAllOrderList
     */
    private void sum(List<ShopDataBean.DataBean.ListBean> mAllOrderList) {
        price = 0;
        count = 0;

        boolean allCheck = true;
        for (ShopDataBean.DataBean.ListBean bean : mAllOrderList) {
            if (bean.isCheck()) {
                //得到总价
                price += bean.getPrice() * bean.getNum();
                //得到商品个数
                count += bean.getNum();
            } else {
                // 只要有一个商品未选中，全选按钮 应该设置成 为选中
                allCheck = false;
            }
        }

        thirdTotalprice.setText("总价: " + price);
        thirdTotalnum.setText("共" + count + "件商品");

        if (allCheck) {
            thirdAllselect.setTag(2);
            thirdAllselect.setBackgroundResource(R.drawable.shopcart_selected);
        } else {
            thirdAllselect.setTag(1);
            thirdAllselect.setBackgroundResource(R.drawable.shopcart_unselected);
        }

    }


    public void getData() {
//            //模拟网络请求
//            InputStream inputStream = getAssets().open("shop.json");
//            final String data = convertStreamToString(inputStream);
//            final Gson gson = new Gson();
//            ShopBean shopBean = gson.fromJson(data, ShopBean.class);
//            for (int i = 0; i < shopBean.getOrderData().size(); i++) {
//                int length = shopBean.getOrderData().get(i).getCartlist().size();
//                for (int j = 0; j < length; j++) {
//                    mAllOrderList.add(shopBean.getOrderData().get(i).getCartlist().get(j));
//                }
//            }

        //网络数据
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("http://120.27.23.105/product/getCarts?uid=100")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Gson gson1 = new Gson();
                List<ShopDataBean.DataBean> data1 = gson1.fromJson(response.body().string(), ShopDataBean.class).getData();
                for (int i = 0; i < data1.size(); i++) {
                    for (int j = 0; j < data1.get(i).getList().size(); j++) {
                        mAllOrderList.add(data1.get(i).getList().get(j));
                    }
                }

                //更新ui,适配器添加数据
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.add(mAllOrderList);
                    }
                });
            }
        });
    }

    public static String convertStreamToString(InputStream is) {
        /*
          * To convert the InputStream to String we use the BufferedReader.readLine()
          * method. We iterate until the BufferedReader return null which means
          * there's no more data to read. Each line will appended to a StringBuilder
          * and returned as String.
          */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    boolean select = false;

    @OnClick(R.id.third_allselect)
    public void onClick() {
        //全选按钮 点击事件

        int tag = (Integer) thirdAllselect.getTag();


        if (tag == 1) {
            thirdAllselect.setTag(2);
            select = true;

        } else {
            thirdAllselect.setTag(1);
            select = false;
        }

        for (ShopDataBean.DataBean.ListBean bean : mAllOrderList) {
            bean.setCheck(select);
        }

//        for (ShopBean.OrderDataBean.CartlistBean bean : mAllOrderList) {
//            bean.setCheck(select);
//        }
        adapter.notifyDataSetChanged();
        sum(adapter.getList());
    }
}

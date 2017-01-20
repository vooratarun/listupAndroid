package sample.listup.com.listupsample.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import sample.listup.com.listupsample.R;
import sample.listup.com.listupsample.models.Book;
import sample.listup.com.listupsample.utils.AppController;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBarcode;
    private Button mISBN;
    private Button allBooks;
    private EditText ISBNText;
    private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private String bookISBN;
    private String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    private LinearLayout priceLayout;
    private EditText priceEdit;
    private Button addBookButton;

    private Book insertingBook;

    private Button testCaseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBarcode = (Button) findViewById(R.id.barcode_scan);
        mISBN = (Button) findViewById(R.id.isbn_code);
        mBarcode.setOnClickListener(this);
        mISBN.setOnClickListener(this);
        allBooks = (Button) findViewById(R.id.all_books);
        allBooks.setOnClickListener(this);
        ISBNText = (EditText) findViewById(R.id.isbn_text);
        priceLayout = (LinearLayout) findViewById(R.id.price_layout);
        priceEdit = (EditText) findViewById(R.id.price);
        addBookButton = (Button) findViewById(R.id.addBook);
        addBookButton.setOnClickListener(this);
        testCaseButton = (Button) findViewById(R.id.testCase);
        testCaseButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.barcode_scan :
                scanBar();
                //scanBarcode();
//                Book book = new Book("El monje que vendió su ferrari",100,
//                        "http://books.google.com/books/content?id=B1DzOQAACAAJ&printsec=frontcover&img=1&zoom=5&source=gbs_api");
               // insertBookInDB(book);
                break;
            case R.id.all_books :
                Intent intent = new Intent(this,ListAllBookActivity.class);
                startActivity(intent);
            case R.id.isbn_code :
                    if(ISBNText.getText().toString().length() > 0) {
                        getBookDetails(ISBNText.getText().toString());
                    } else {
                        Toast.makeText(this, "Please Enter code.", Toast.LENGTH_SHORT).show();
                    }
                break;

            case R.id.addBook :
                String priceText = priceEdit.getText().toString();
                if(priceText.length() > 0 ){
                    insertingBook.setBookPrice(Integer.parseInt(priceText));
                    insertBookInDB(insertingBook);
                } else {
                    Toast.makeText(this, "Please Enter priceEdit..", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.testCase :
                bookISBN = "9780759521438";
                getBookDetails("9780759521438");
                break;
            default:
                break;
        }
    }

    //product barcode mode
    public void scanBar() {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final Activity act, CharSequence title,
                                          CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(act, "cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        return downloadDialog.show();
    }

        //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Log.d("ScanResult",contents);
                getBookDetails(format);
            }
        }
    }
    private void getBookDetails(String bookISBN) {

        String url = GOOGLE_BOOKS_API+bookISBN;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    int totalItems = response.getInt("totalItems");
                    if(totalItems > 0) {
                        JSONObject item = response.getJSONArray("items").getJSONObject(0);
                        String title = item.getJSONObject("volumeInfo").getString("title");
                        String imageUrl = item.getJSONObject("volumeInfo").getJSONObject("imageLinks").
                                getString("smallThumbnail");
                        priceLayout.setVisibility(View.VISIBLE);
                        addBookButton.setVisibility(View.VISIBLE);
                        insertingBook  = new Book(title,0,imageUrl);
                    }else {
                        Toast.makeText(MainActivity.this, "No book found with this ISBN", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        AppController.getInstance().addToRequestQueue(request);
    }

    private void insertBookInDB(Book book){

        Toast.makeText(this, "inserting book", Toast.LENGTH_SHORT).show();
        JSONObject object = new JSONObject();
        try {
            object.put("bookImage",book.getBookImage());
            object.put("bookName",book.getBookTitle());
            object.put("bookPrice",book.getBookPrice());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "http://52.74.62.47:3000/create/book";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,
                object,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Toast.makeText(MainActivity.this, response.getString("result"), Toast.LENGTH_SHORT).show();
                    priceLayout.setVisibility(View.GONE);
                    addBookButton.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());
                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
        AppController.getInstance().addToRequestQueue(request);
    }


    //    public void scanBarcode() {
//        new IntentIntegrator(this).initiateScan();
//    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        if(result != null) {
//            if(result.getContents() == null) {
//                Log.d("MainActivity", "Cancelled scan");
//                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
//            } else {
//                Log.d("MainActivity", "Scanned");
//                bookISBN = result.getContents();
//                getBookDetails(bookISBN);
//                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
//            }
//        } else {
//            // This is important, otherwise the result will not be passed to the fragment
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }

}
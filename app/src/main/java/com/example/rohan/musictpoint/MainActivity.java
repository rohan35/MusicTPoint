package com.example.rohan.musictpoint;

import android.Manifest;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Properties;


public class MainActivity extends AppCompatActivity {
    SwipeRefreshLayout mySwipeRefreshLayout;
    TextView textView;
    ListView songView;
    ArrayList<Song> songList = new ArrayList<Song>();
    ResourceId rId;
    ProgressBar progressBar;
    SongAdapter songAdt;
    ImageView download;
    Song song;
    Button retry;
    String pageToken = "", pageToken2 = "";
    int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.NoConnection);
        retry = (Button) findViewById(R.id.tryAgain);
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        } else {

            new RetrieveFeedTask().execute();}

        //  mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        songView = (ListView) findViewById(R.id.activity_main);
        songView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int currentVisibleItemCount;
            private int currentScrollState;
            private int currentFirstVisibleItem;
            private int totalItem;
            private LinearLayout lBelow;
            // Called when the scroll bar reaches to the end

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                this.currentScrollState = scrollState;
                this.isScrollCompleted();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
                this.totalItem = totalItemCount;


            }

            private void isScrollCompleted() {
                if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                        && this.currentScrollState == SCROLL_STATE_IDLE) {
                    /** To do code here*/
                    currentPosition++;
                    new RetrieveFeedTask().execute();


                }

            }
        });
        // Used when  the item in the songView is clicked this will redirect the person to youtube
        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                song = songList.get(position);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + song.getId()));
                startActivity(intent);
            }
        });
// Check the internet Connection
        if (!isNetworkConnected()) {
            textView.setVisibility(View.VISIBLE);
            retry.setVisibility(View.VISIBLE);
        }
      /*  mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new RetrieveFeedTask().execute();
                    }
                }
        );*/
    }
//Retry Button code
    public void tryAgain(View v) {
        if (!isNetworkConnected()) {

            textView.setVisibility(View.VISIBLE);
            retry.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
            retry.setVisibility(View.GONE);
            new RetrieveFeedTask().execute();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
// Download Code for downloading the audio file

    public void download(View v) {
        try {

        View parentRow = (View) v.getParent();
        View grandParent = (View) parentRow.getParent();
        ListView listView = (ListView) grandParent.getParent();

        final int position = listView.getPositionForView(parentRow);



        DownloadManager mManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request mRqRequest = new DownloadManager.Request(
                Uri.parse("http://www.youtubeinmp3.com/fetch/?video=https://www.youtube.com/watch?v=" + songList.get(position).getId()));

        mRqRequest.allowScanningByMediaScanner();


        mRqRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        mRqRequest.setDestinationInExternalgPublicDir(Environment.DIRECTORY_DOWNLOADS, songList.get(position).getTitle() + ".mp3");

//  mRqRequest.setDestinationUri(Uri.parse("give your local path"));
        mManager.enqueue(mRqRequest);
    }catch(Exception e)
        {
            Toast.makeText(this, "Cannot download please try again after sometime", Toast.LENGTH_SHORT).show();
        }
    }
// Youtube api to get the videos of music
    String queryTerm = "";
    int count = 0;
    YouTube.Search.List search;
    private static final String PROPERTIES_FILENAME = "youtube.properties";
    private static YouTube youtube;
    private static final long NUMBER_OF_VIDEOS_RETURNED = 50;


    class RetrieveFeedTask extends AsyncTask<Void, Void, ArrayList<Song>> {
        @Override
        protected void onPreExecute() {
            // Here you can show progress bar or something on the similar lines.
            // Since you are in a UI thread here.
            progressBar = (ProgressBar) findViewById(R.id.progressbar);
            progressBar.setVisibility(View.VISIBLE);

        }

        protected ArrayList<Song> doInBackground(Void... urls) {
            Properties properties = new Properties();


            try {


                    youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                        public void initialize(HttpRequest request) throws IOException {
                        }
                    }).setApplicationName("youtube-cmdline-search-sample").build();

                    // Prompt the user to enter a query term.


                    // Define the API request for retrieving search results.
                    search = youtube.search().list("id,snippet");

                    // Set your developer key from the Google Developers Console for
                    // non-authenticated requests. See:
                    // https://console.developers.google.com/
                    String apiKey = "AIzaSyAkkLiYQ2Rm4NMkT41nvh9tZG3jx8mFuyw";
                    search.setKey(apiKey);

                    search.setVideoCategoryId("10");
                    if (pageToken != "") {
                        search.setPageToken(pageToken);
                    }
                    // Restrict the search results to only include videos. See:
                    // https://developers.google.com/youtube/v3/docs/search/list#type
                    search.setType("video");

                    // To increase efficiency, only retrieve the fields that the
                    // application uses.
                    //search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
                    search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

                    // Call the API and print results.
                    SearchListResponse searchResponse = search.execute();
                    pageToken = searchResponse.getNextPageToken();
                    System.out.println(pageToken);

                    List<SearchResult> searchResultList = searchResponse.getItems();
                    ListIterator<SearchResult> it = searchResultList.listIterator();
                    if (searchResultList != null) {

                        while (it.hasNext()) {

                            SearchResult singleVideo = it.next();
                            rId = singleVideo.getId();

                            // Confirm that the result represents a video. Otherwise, the
                            // item will not contain a video ID.
                            if (rId.getKind().equals("youtube#video")) {
                                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();


                                songList.add(new Song(rId.getVideoId(), singleVideo.getSnippet().getTitle(), thumbnail.getUrl()));


                            }
                        }



                    count = 1;
                }
                return songList;
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(ArrayList<Song> response) {

            progressBar.setVisibility(View.GONE);
            //mySwipeRefreshLayout.setRefreshing(false);

            songAdt = new SongAdapter(MainActivity.this, songList);
            songView.setTextFilterEnabled(true);
            songView.setAdapter(songAdt);

            if (songView.getCount() > 50) {


                songView.setSelection(songView.getCount() - 50);
            }

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    new RetrieveFeedTask().execute();

                } else {
                    Toast.makeText(this, "Please run the Application again and accept the Permission", Toast.LENGTH_SHORT).show();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
// Search the term code
    class SearchFeedTask extends AsyncTask<Void, Void, ArrayList<Song>> {
        @Override
        protected void onPreExecute() {
            // Here you can show progress bar or something on the similar lines.
            // Since you are in a UI thread here.
            progressBar = (ProgressBar) findViewById(R.id.progressbar);
            progressBar.setVisibility(View.VISIBLE);

        }

        protected ArrayList<Song> doInBackground(Void... urls) {
            Properties properties = new Properties();


            try {
                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {


                    youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                        public void initialize(HttpRequest request) throws IOException {
                        }
                    }).setApplicationName("youtube-cmdline-search-sample").build();

                    // Prompt the user to enter a query term.


                    // Define the API request for retrieving search results.
                    search = youtube.search().list("id,snippet");

                    // Set your developer key from the Google Developers Console for
                    // non-authenticated requests. See:
                    // https://console.developers.google.com/
                    String apiKey = "AIzaSyAkkLiYQ2Rm4NMkT41nvh9tZG3jx8mFuyw";
                    search.setKey(apiKey);
                    search.setQ(queryTerm);

                    if (pageToken2 != "") {
                        search.setPageToken(pageToken2);
                    }
                    // Restrict the search results to only include videos. See:
                    // https://developers.google.com/youtube/v3/docs/search/list#type
                    search.setType("video");

                    // To increase efficiency, only retrieve the fields that the
                    // application uses.
                    //search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
                    search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

                    // Call the API and print results.
                    SearchListResponse searchResponse = search.execute();
                    pageToken2 = searchResponse.getNextPageToken();
                    System.out.println(pageToken);

                    List<SearchResult> searchResultList = searchResponse.getItems();
                    ListIterator<SearchResult> it = searchResultList.listIterator();
                    if (searchResultList != null) {

                        while (it.hasNext()) {

                            SearchResult singleVideo = it.next();
                            ResourceId rId2 = singleVideo.getId();

                            // Confirm that the result represents a video. Otherwise, the
                            // item will not contain a video ID.
                            if (rId2.getKind().equals("youtube#video")) {
                                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();


                                songList.add(new Song(rId2.getVideoId(), singleVideo.getSnippet().getTitle(), thumbnail.getUrl()));


                            }
                        }
                    }


                    count = 1;
                }
                return songList;
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(ArrayList<Song> response) {

            progressBar.setVisibility(View.GONE);
            //mySwipeRefreshLayout.setRefreshing(false);

            songAdt = new SongAdapter(MainActivity.this, songList);
            songView.setTextFilterEnabled(true);
            songView.setAdapter(songAdt);

            if (songView.getCount() > 50) {


                songView.setSelection(songView.getCount() - 50);
            }
            //progressBar.setVisibility(View.GONE);


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem menuItem) {
                        // Return true to allow the action view to expand
                        System.out.println("1");
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                        // When the action view is collapsed, reset the query
                        System.out.println("2");
                        new RetrieveFeedTask().execute();
                        // Return true to allow the action view to collapse
                        return true;
                    }
                });

        //*** setOnQueryTextFocusChangeListener ***\
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String searchQuery) {
                if (searchQuery.length() > 3) {
                    songList.clear();
                    queryTerm = searchQuery;
                    new SearchFeedTask().execute();

                }


                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {

                return false;
            }


        });
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when collapsed
                songList.clear();
                new RetrieveFeedTask().execute();
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        });
        return true;
    }
   /* @Override
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            for (int i=0;i<songList.size();i++)
            {
                Log.v("songlist1",songList.toString());
                if (songList.get(i).getTitle().contains(query))
                {
                    Log.v("songlist",songList.toString());
                songList2.add( new Song(song.getId(),song.getTitle(),song.getPath()));

                }
                else
                {
                    Log.v("songlist",songList.toString());
                }

            }



        }
        }*/
}




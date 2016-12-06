package com.example.rohan.musictpoint.fetchers;

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

            songAdapter = new SongRecyclerAdapter(MainActivity.this, songList);
            songView.setTextFilterEnabled(true);
            songView.setAdapter(songAdapter);

            if (songView.getCount() > 50) {


                songView.setSelection(songView.getCount() - 50);
            }

        }
    }
public class AlbumsProfile {
    private String artist;
    private String title;
    private String year;

    public AlbumsProfile(String artist, String title, String year) {
        this.artist = artist;
        this.title = title;
        this.year = year;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(String year) {
        this.year = year;
    }
}

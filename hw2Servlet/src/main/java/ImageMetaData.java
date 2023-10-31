public class ImageMetaData {
    private String albumId;
    private String imageSize;

    public ImageMetaData(String albumId, String imageSize) {
        this.albumId = albumId;
        this.imageSize = imageSize;
    }

    public String getAlbumId() {
        return albumId;
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }
}

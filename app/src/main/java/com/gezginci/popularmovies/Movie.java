package com.gezginci.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    String id;
    String name;
    String coverUrl;
    String plot;
    String releaseDate;
    int rank;
    float rating;

    public Movie(Movie item) {
        this.id = item.id;
        this.name = item.name;
        this.coverUrl = item.coverUrl;
        this.rank = item.rank;
        this.plot = item.plot;
        this.releaseDate = item.releaseDate;
        this.rating = item.rating;
    }

    public Movie(String id, String name, String coverUrl, String plot, int rank, String releaseDate, float rating) {
        this.id = id;
        this.name = name;
        this.coverUrl = coverUrl;
        this.plot = plot;
        this.rank = rank;
        this.releaseDate = releaseDate;
        this.rating = rating;
    }

    private Movie(Parcel in) {
        id = in.readString();
        name = in.readString();
        coverUrl = in.readString();
        plot = in.readString();
        rank = in.readInt();
        releaseDate = in.readString();
        rating = in.readFloat();
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(coverUrl);
        out.writeString(plot);
        out.writeInt(rank);
        out.writeString(releaseDate);
        out.writeFloat(rating);
    }

}

package dao;

import entity.aktor;
import entity.filmler;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class filmlerDAO extends superDAO {

    PreparedStatement pst = null;
    ResultSet rs = null;

    private kategorilerDAO kdao;
    private aktorDAO adao;
    private dosyaDAO ddao;

    public void insert(filmler film) {  // filmler içerisine veri eklenen metod.

        try {
            pst = this.getConnection().prepareStatement("insert into filmler (film_isim,film_tanimi,cikis_yili,yonetmen, kategori_id,imbd,fragman,dosya_id)"
                    + " values (?,?,?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            pst.setString(1, film.getFilm_isim());
            pst.setString(2, film.getFilm_tanimi());
            pst.setInt(3, film.getCikis_yili());
            pst.setString(4, film.getYonetmen());
            pst.setInt(5, film.getKategori().getKategori_id());
            pst.setDouble(6, film.getImbd());
            pst.setString(7, film.getFragman());
            pst.setInt(8, film.getDosya().getId());

            pst.executeUpdate();
//******************************************************Veritabanındaki Film_aktor tablosuna insert işlemi  ************************
            int film_id = 0;
            ResultSet gk = pst.getGeneratedKeys();

            if (gk.next()) {
                film_id = gk.getInt(1);
            }

            for (aktor ak : film.getFilmAktor()) {

                pst = this.getConnection().prepareStatement("INSERT INTO  film_aktor (film_id,aktor_id) values (?,?)");
                pst.setInt(1, film_id);
                pst.setInt(2, ak.getAktor_id());
                pst.executeUpdate();
            }
        } catch (SQLException ex) {
            System.out.println(" FilmlerDAO HATA(Create): " + ex.getMessage());
        }
    }

    public void delete(filmler film) {

        try {
            pst = this.getConnection().prepareStatement("DELETE FROM filmler WHERE film_id=?");
            pst.setInt(1, film.getFilm_id());
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            System.out.println(" FilmlerDAO HATA(Delete): " + ex.getMessage());
        }
    }

    public List<filmler> findAll(String deger, int page, int pageSize) {

        List<filmler> flist = new ArrayList();
        int start = (page - 1) * pageSize;
        try {

            pst = this.getConnection().prepareStatement("SELECT * FROM filmler where film_isim like ? order by imbd DESC limit " + start + "," + pageSize);
            pst.setString(1, "%" + deger + "%"); // bul metodu için string değeri set ettiğimiz kod satırı.

            rs = pst.executeQuery();
            while (rs.next()) {
                filmler temp = new filmler();

                temp.setFilm_id(rs.getInt("film_id"));
                temp.setFilm_isim(rs.getString("film_isim"));
                temp.setFilm_tanimi(rs.getString("film_tanimi"));
                temp.setCikis_yili(rs.getInt("cikis_yili"));
                temp.setYonetmen(rs.getString("yonetmen"));
                temp.setImbd(rs.getDouble("imbd"));
                temp.setFragman(rs.getString("fragman"));
                temp.setDosya(this.getDdao().find(rs.getInt("dosya_id")));
                temp.setKategori(this.getKdao().find(rs.getInt("kategori_id")));
                temp.setFilmAktor(this.getAdao().getFilmAktor(rs.getInt("film_id")));

                flist.add(temp);
            }
            //System.out.println("***************************Liste Boyutu:"+flist.get(0).getFilmAktor().toString());
        } catch (SQLException ex) {
            System.out.println("filmlerDAO HATA(ReadAll):" + ex.getMessage());
        }
        return flist;
    }
    //Yorumlar ve puanlar form kısmında full listeyi görebilmek için yazılmış kod
    public List<filmler> fullFilm() {

        List<filmler> flist = new ArrayList();

        try {
            pst = this.getConnection().prepareStatement("SELECT * FROM filmler order by film_isim DESC");
            rs = pst.executeQuery();
            while (rs.next()) {
                filmler temp = new filmler();

                temp.setFilm_id(rs.getInt("film_id"));
                temp.setFilm_isim(rs.getString("film_isim"));
                temp.setFilm_tanimi(rs.getString("film_tanimi"));
                temp.setCikis_yili(rs.getInt("cikis_yili"));
                temp.setYonetmen(rs.getString("yonetmen"));
                temp.setImbd(rs.getDouble("imbd"));
                temp.setFragman(rs.getString("fragman"));
                temp.setDosya(this.getDdao().find(rs.getInt("dosya_id")));
                temp.setKategori(this.getKdao().find(rs.getInt("kategori_id")));
                temp.setFilmAktor(this.getAdao().getFilmAktor(rs.getInt("film_id")));

                flist.add(temp);
            }
        } catch (SQLException ex) {
            System.out.println("filmlerDAO HATA(ReadAll):" + ex.getMessage());
        }
        return flist;
    }

    public int count() {

        int count = 0;
        try {

            pst = this.getConnection().prepareStatement("SELECT count(film_id) as film_count from filmler ");
            rs = pst.executeQuery();
            rs.next();
            count = rs.getInt("film_count");

        } catch (SQLException ex) {
            System.out.println("filmlerDAO HATA(ReadAll):" + ex.getMessage());
        }
        return count;
    }

    public void update(filmler f) {

        try {

            pst = this.getConnection().prepareStatement("UPDATE filmler SET film_isim=?,film_tanimi=?,cikis_yili=?,"
                    + "yonetmen=?,kategori_id=?, imbd=?, fragman=?, dosya_id=? where film_id=?");
            pst.setString(1, f.getFilm_isim());
            pst.setString(2, f.getFilm_tanimi());
            pst.setInt(3, f.getCikis_yili());
            pst.setString(4, f.getYonetmen());
            pst.setInt(5, f.getKategori().getKategori_id());
            pst.setDouble(6, f.getImbd());
            pst.setString(7, f.getFragman());
            pst.setInt(8, f.getDosya().getId());
            pst.setInt(9, f.getFilm_id());

            pst.executeUpdate();

            //Önce 3. tablodan servisleri siliyoruz.
            pst = this.getConnection().prepareStatement("DELETE FROM film_aktor where film_id=?");
            pst.setInt(1, f.getFilm_id());
            pst.executeUpdate();
            //Burada tekrar ekliyoruz.
            for (aktor ak : f.getFilmAktor()) {

                pst = this.getConnection().prepareStatement("INSERT INTO  film_aktor (film_id,aktor_id) values (?,?)");
                pst.setInt(1, f.getFilm_id());
                pst.setInt(2, ak.getAktor_id());
                pst.executeUpdate();
            }

        } catch (SQLException ex) {
            System.out.println("filmlerDAO HATA(Update): " + ex.getMessage());
        }
    }

    public filmler find(int id) {

        filmler temp = null;
        try {

            pst = this.getConnection().prepareStatement("SELECT * FROM filmler WHERE film_id=?");
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                temp = new filmler();
                temp.setFilm_id(rs.getInt("film_id"));
                temp.setFilm_isim(rs.getString("film_isim"));
                temp.setFilm_tanimi(rs.getString("film_tanimi"));
                temp.setCikis_yili(rs.getInt("cikis_yili"));
                temp.setYonetmen(rs.getString("yonetmen"));
                temp.setImbd(rs.getDouble("imbd"));
                temp.setFragman(rs.getString("fragman"));
                temp.setDosya(this.getDdao().find(rs.getInt("dosya_id")));
                temp.setKategori(this.getKdao().find(rs.getInt("kategori_id")));
                temp.setFilmAktor(this.getAdao().getFilmAktor(rs.getInt("film_id")));   //Buraya dikkat buradan patlayabilir!
            }

        } catch (SQLException ex) {
            System.out.println("filmlerDAO HATA(FİND) :" + ex.getMessage());
        }
        return temp;
    }

    public kategorilerDAO getKdao() {
        if (this.kdao == null) {
            this.kdao = new kategorilerDAO();
        }
        return kdao;
    }

    public void setKdao(kategorilerDAO kdao) {
        this.kdao = kdao;
    }

    public aktorDAO getAdao() {
        if (this.adao == null) {
            this.adao = new aktorDAO();
        }
        return adao;
    }

    public void setAdao(aktorDAO adao) {
        this.adao = adao;
    }

    public dosyaDAO getDdao() {
        if (this.ddao == null) {
            this.ddao = new dosyaDAO();
        }
        return ddao;
    }

    public void setDdao(dosyaDAO ddao) {
        this.ddao = ddao;
    }

}

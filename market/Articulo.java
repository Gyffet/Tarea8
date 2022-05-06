package market;

import com.google.gson.*;

public class Articulo
{
  String descripcion;
  float precio;
  int cantidad;
  byte[] foto;

  // @FormParam necesita un metodo que convierta una String al objeto de tipo Usuario
  public static Articulo valueOf(String s) throws Exception
  {
    Gson j = new GsonBuilder().registerTypeAdapter(byte[].class,new AdaptadorGsonBase64()).create();
    return (Articulo)j.fromJson(s,Articulo.class);
  }
}

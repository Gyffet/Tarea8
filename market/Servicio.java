package market;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Response;

import java.sql.*;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;

import java.util.ArrayList;
import com.google.gson.*;



@Path("ws")
public class Servicio
{
  static DataSource pool = null;
  static
  {		
    try
    {
      Context ctx = new InitialContext();
      pool = (DataSource)ctx.lookup("java:comp/env/jdbc/datasource_Servicio");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  static Gson j = new GsonBuilder()
		.registerTypeAdapter(byte[].class,new AdaptadorGsonBase64())
		.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
		.create();

  @POST
    @Path("Nuevo_Articulo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response alta(@FormParam("articulo") Articulo articulo) throws Exception {
        Connection conexion = pool.getConnection();

        try {
            PreparedStatement stmt_2 = conexion.prepareStatement("INSERT INTO articulos VALUES (0,?,?,?)");
            try {
                stmt_2.setString(1, articulo.descripcion);
                stmt_2.setFloat(2, articulo.precio);
                stmt_2.setInt(3, articulo.cantidad);
                stmt_2.executeUpdate();
            } finally {
                stmt_2.close();
            }
        } catch (Exception e) {
            return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
        } finally {
            conexion.close();
        }
        return Response.ok().build();

    }

    @POST
    @Path("Consulta_Articulo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response consulta(@FormParam("descripion") String descripcion) throws Exception {
        Connection conexion = pool.getConnection();

        try {
            PreparedStatement stmt_1 = conexion.prepareStatement("SELECT a.descripcion, a.precio, a.almacen WHERE descripcion LIKE %?%");
            try {
                stmt_1.setString(1, descripcion);

                ResultSet rs = stmt_1.executeQuery();
                try {
                    if (rs.next()) {
                        Articulo r = new Articulo();
                        r.descripcion = rs.getString(1);
                        r.precio = rs.getInt(2);
                        r.cantidad = rs.getInt(3);
//	      r.foto = rs.getBytes(8);
                        return Response.ok().entity(j.toJson(r)).build();
                    }
                    return Response.status(400).entity(j.toJson(new Error("No se encontro el art??culo"))).build();
                } finally {
                    rs.close();
                }
            } finally {
                stmt_1.close();
            }
        } catch (Exception e) {
            return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
        } finally {
            conexion.close();
        }
    }

    @POST
    @Path("alCarrito")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response alCarro(@FormParam("articulo") Articulo articulo) throws Exception {
        Connection conexion = pool.getConnection();
        try {
            PreparedStatement stmt = conexion.prepareStatement("SELECT a.cantidad FROM articulos WHERE cantidad=articulo.cantidad");
            PreparedStatement stmt_1 = conexion.prepareStatement("UPDATE articulo SET cantidad=? WHERE descripcion=?");
            PreparedStatement stmt_2 = conexion.prepareStatement("INSERT INTO carrito VALUES (0,?,?,?)");
            int resta = 0;
            try {
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {

                    resta = rs.getInt(3) - articulo.cantidad;
                    stmt_1.setInt(1, resta);
                    stmt_1.setString(2, articulo.descripcion);
                    stmt_1.executeUpdate();

                    stmt_2.setString(1, articulo.descripcion);
                    stmt_2.setFloat(2, articulo.precio);
                    stmt_2.setInt(3, articulo.cantidad);
                    stmt_2.executeUpdate();

                }
                return Response.status(400).entity(j.toJson(new Error("Error al rescatar cantidad"))).build();

            } catch (Exception e) {
                return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
            } finally {
                conexion.close();
                stmt.close();
                stmt_1.close();
                stmt_2.close();
                return Response.ok()
                        .build();
            }

        } catch (Exception e) {
            return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
        }
    }

    @POST
    @Path("total_carrito")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response totalCarro(@FormParam("articulo") Articulo articulo) throws Exception {
        Connection conexion = pool.getConnection();

        try {
            PreparedStatement stmt_1 = conexion.prepareStatement("SELECT * FROM carrito");
            try {

                ResultSet rs = stmt_1.executeQuery();
                try {
                    if (!rs.next()) {
                        return Response.status(400).entity(j.toJson(new Error("El carrito esta vacio"))).build();
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt_1.close();
            }
        } catch (Exception e) {
            return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
        } finally {
            conexion.close();
        }
        return Response.ok().build();
    }

    @POST
    @Path("eliminar_Articulo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response elimina(@FormParam("descripcion") String descripcion) throws Exception {
        Connection conexion = pool.getConnection();

        try {
            PreparedStatement stmt_1 = conexion.prepareStatement("DELETE FROM carrito WHERE descripcion=?");
            try {
                stmt_1.setString(1, descripcion);
                ResultSet rs = stmt_1.executeQuery();

            } catch (Exception e) {
                return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
            } finally {
                stmt_1.close();
            }

        } catch (Exception e) {
            return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
        } finally {
            conexion.close();
        }
        return Response.ok().build();
    }

    @POST
    @Path("eliminar_todo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminaTodo(@FormParam("descripcion") String descripcion) throws Exception {
        Connection conexion = pool.getConnection();

        try {
            PreparedStatement stmt_1 = conexion.prepareStatement("DELETE FROM carrito;");
            try {
                stmt_1.executeQuery();
            } catch (Exception e) {
                return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
            } finally {
                stmt_1.close();
            }

        } catch (Exception e) {
            return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
        } finally {
            conexion.close();
        }
        return Response.ok().build();
    }
}

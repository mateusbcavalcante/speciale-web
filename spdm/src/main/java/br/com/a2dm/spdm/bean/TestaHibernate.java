package br.com.a2dm.spdm.bean;

import java.sql.DriverManager;
import java.sql.SQLException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import br.com.a2dm.brcmn.entity.Usuario;
import br.com.a2dm.brcmn.service.UsuarioService;
import br.com.a2dm.brcmn.util.jsf.AbstractBean;

@RequestScoped
@ManagedBean
public class TestaHibernate extends AbstractBean<Usuario, UsuarioService> {
	public static void main(String[] args) throws SQLException {
		try {
			Class.forName("org.postgresql.Driver");
			DriverManager.getConnection("jdbc:postgresql://189.126.111.74:5432/ped", "ped", "@Socio2016");
			System.out.println("Conectado");
		} catch (ClassNotFoundException e) {
			System.out.println("Não conectado");
			throw new SQLException(e.getMessage());
		}
	}
}
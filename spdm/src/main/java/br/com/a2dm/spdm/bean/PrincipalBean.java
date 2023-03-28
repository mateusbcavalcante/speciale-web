package br.com.a2dm.spdm.bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.spdm.config.MenuControl;
import br.com.a2dm.spdm.entity.Aviso;
import br.com.a2dm.spdm.service.AvisoService;

@RequestScoped
@ManagedBean
public class PrincipalBean extends AbstractBean<Aviso, AvisoService> {
	
	List<Aviso> listAvisos;
	
	public PrincipalBean() throws Exception
	{
		super(AvisoService.getInstancia());
		this.ACTION_SEARCH = "principal";
		this.pageTitle = "Tela Início";
		
		MenuControl.ativarMenu("flgMenuMan");
		MenuControl.ativarSubMenu("flgMenuManCli");
		
		this.listarAvisos();
	}
	
	private void listarAvisos() throws Exception {
		try {
			Aviso aviso = new Aviso();
			aviso.setAtivo(true);
			
			List<Aviso> listAvisosAtivos = AvisoService.getInstancia().pesquisar(aviso, 0);
			List<Aviso> listAvisosAtivosNaData = new ArrayList<Aviso>();
			
			Calendar cal = Calendar.getInstance();
		    cal.setTime(new Date());
		    cal.set(Calendar.HOUR_OF_DAY, 0);
		    cal.set(Calendar.MINUTE, 0);
		    cal.set(Calendar.SECOND, 0);
		    cal.set(Calendar.MILLISECOND, 0);
			Date dataAtual = cal.getTime();
			
			for (Aviso avisoAtivo : listAvisosAtivos) {
				if (dataAtual.compareTo(avisoAtivo.getDat_inicial()) >= 0 && dataAtual.compareTo(avisoAtivo.getDat_final()) <= 0) {
					listAvisosAtivosNaData.add(avisoAtivo);
				}
			}
			
			setListAvisos(listAvisosAtivosNaData);
		} catch (Exception e) {
			System.out.println("Erro na consulta de avisos");
		}
	}

	public List<Aviso> getListAvisos() {
		return listAvisos;
	}

	public void setListAvisos(List<Aviso> listAvisos) {
		this.listAvisos = listAvisos;
	}
}

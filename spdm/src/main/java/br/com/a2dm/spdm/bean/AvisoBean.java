package br.com.a2dm.spdm.bean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.brcmn.util.jsf.Variaveis;
import br.com.a2dm.spdm.config.MenuControl;
import br.com.a2dm.spdm.entity.Aviso;
import br.com.a2dm.spdm.entity.Cliente;
import br.com.a2dm.spdm.service.AvisoService;
import br.com.a2dm.spdm.service.ClienteService;

@RequestScoped
@ManagedBean
public class AvisoBean extends AbstractBean<Aviso, AvisoService>{
	
	private LocalDate dataAtual;
	private List<Aviso> avisos;
	
	public AvisoBean()
	{
		super(AvisoService.getInstancia());
		this.ACTION_SEARCH = "aviso";
		this.pageTitle = "Avisos";
		
		MenuControl.ativarMenu("flgMenuMan");
		MenuControl.ativarSubMenu("flgMenuManCli");
	}
	
	@Override
	protected void setValoresDefault() throws Exception
	{
			
		Aviso aviso = new Aviso();	
		List<Aviso> resultCli = AvisoService.getInstancia().pesquisar(aviso, 0);
		
		System.out.println("result" + resultCli);
		
		this.setAvisos(resultCli);
		
		System.out.println("avisos" + avisos);
		
		this.setDataAtual(LocalDate.now());
	}
	
	public LocalDate getDataAtual() {
		return dataAtual;
	}

	public void setDataAtual(LocalDate dataAtual) {
		this.dataAtual = dataAtual;
	}

	public List<Aviso> getAvisos() {
		return avisos;
	}

	public void setAvisos(List<Aviso> avisos) {
		this.avisos = avisos;
	}

	@Override
	protected void completarInserir() throws Exception
	{
		this.getEntity().setAtivo(true);
	}
	
	public void ativar() 
	{		
		try
		{
			if(this.getEntity() != null)
			{
				if(validarAcesso(Variaveis.ACAO_ATIVAR))
				{
					AvisoService.getInstancia().ativar(this.getEntity());
					
					FacesMessage message = new FacesMessage("Registro ativado com sucesso!");
					message.setSeverity(FacesMessage.SEVERITY_INFO);
					FacesContext.getCurrentInstance().addMessage(null, message);
				}
			}
		}
		catch (Exception e) 
		{
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}		
	}
	
	public void inativar() 
	{		
		try
		{
			if(this.getEntity() != null)
			{
				if(validarAcesso(Variaveis.ACAO_INATIVAR))
				{
					AvisoService.getInstancia().inativar(this.getEntity());
					FacesMessage message = new FacesMessage("Registro inativado com sucesso!");
					message.setSeverity(FacesMessage.SEVERITY_INFO);
					FacesContext.getCurrentInstance().addMessage(null, message);
				}
			}
		}
		catch (Exception e) 
		{
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}		
	}
	
}

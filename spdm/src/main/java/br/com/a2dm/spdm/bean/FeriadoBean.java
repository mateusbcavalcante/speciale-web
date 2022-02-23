package br.com.a2dm.spdm.bean;

import java.io.IOException;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletResponse;

import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.brcmn.util.jsf.JSFUtil;
import br.com.a2dm.brcmn.util.jsf.Variaveis;
import br.com.a2dm.brcmn.util.validacoes.ValidaPermissao;
import br.com.a2dm.spdm.config.MenuControl;
import br.com.a2dm.spdm.entity.Feriado;
import br.com.a2dm.spdm.service.FeriadoService;

@RequestScoped
@ManagedBean
public class FeriadoBean extends AbstractBean<Feriado, FeriadoService>
{
	private JSFUtil util = new JSFUtil();
	
	public FeriadoBean()
	{
		super(FeriadoService.getInstancia());
		this.ACTION_SEARCH = "feriado";
		this.pageTitle = "Manutenção / Feriado";
		
		MenuControl.ativarMenu("flgMenuMan");
		MenuControl.ativarSubMenu("flgMenuManUsr");
	}
	
	@Override
	protected void completarPesquisar() throws Exception
	{
		if(this.getSearchObject().getFlgAtivo().equals("T"))
		{
			this.getSearchObject().setFlgAtivo(null);
		}
	}
	
	@Override
	protected void setValoresDefault() throws Exception
	{
		this.getSearchObject().setFlgAtivo("T");
	}
	
	@Override
	protected void validarInserir() throws Exception
	{
		if(this.getEntity().getDesFeriado() == null || this.getEntity().getDesFeriado().trim().equals(""))
		{
			throw new Exception("O campo Nome é obrigatório.");
		}
		
		if(this.getEntity().getDatFeriado() == null)
		{
			throw new Exception("O campo Data do Feriado é obrigatório.");
		}
	}

	@Override
	protected void completarInserir() throws Exception
	{
		this.getEntity().setDatCadastro(new Date());
		this.getEntity().setFlgAtivo("S");
		this.getEntity().setIdUsuarioCad(util.getUsuarioLogado().getIdUsuario());
	}
	
	@Override
	public void preparaAlterar() 
	{
		try
		{
			if(validarAcesso(Variaveis.ACAO_PREPARA_ALTERAR))
			{
				super.preparaAlterar();
				Feriado feriado = new Feriado();
				feriado.setIdFeriado(getEntity().getIdFeriado());
				feriado = FeriadoService.getInstancia().get(feriado, FeriadoService.JOIN_USUARIO_CAD
						   										   | FeriadoService.JOIN_USUARIO_ALT);
				
				setEntity(feriado);
			}
		}
	    catch (Exception e)
	    {
	       FacesMessage message = new FacesMessage(e.getMessage());
	       message.setSeverity(FacesMessage.SEVERITY_ERROR);
	       FacesContext.getCurrentInstance().addMessage(null, message);
	    }
	}
	
	@Override
	protected void completarAlterar() throws Exception 
	{
		this.validarInserir();
		
		this.getEntity().setDatAlteracao(new Date());
		this.getEntity().setIdUsuarioAlt(util.getUsuarioLogado().getIdUsuario());
	}
	
	public void ativar() 
	{		
		try
		{
			if(this.getEntity() != null)
			{
				if(validarAcesso(Variaveis.ACAO_ATIVAR))
				{
					FeriadoService.getInstancia().ativar(this.getEntity());
					
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
					FeriadoService.getInstancia().inativar(this.getEntity());
					
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
	
	public void visualizar()
	{
		try
		{
			Feriado feriado = new Feriado();
			feriado.setIdFeriado(this.getEntity().getIdFeriado());
			
			feriado = FeriadoService.getInstancia().get(feriado, FeriadoService.JOIN_USUARIO_CAD
					                                           | FeriadoService.JOIN_USUARIO_ALT);
			
			this.setEntity(feriado);
		}
		catch (Exception e) 
		{
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
	
	@Override
	protected boolean validarAcesso(String acao)
	{
		boolean temAcesso = true;

		if (!ValidaPermissao.getInstancia().verificaPermissao("usuario", acao))
		{
			temAcesso = false;
			HttpServletResponse rp = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
			try
			{
				rp.sendRedirect("/spdm/pages/acessoNegado.jsf");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		return temAcesso;
	}
	
	@Override
	public void cancelar(ActionEvent event)
	{
		super.cancelar(event);
		this.getSearchObject().setFlgAtivo("T");
	}
}
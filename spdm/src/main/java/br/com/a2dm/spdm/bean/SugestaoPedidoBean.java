package br.com.a2dm.spdm.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.spdm.config.MenuControl;
import br.com.a2dm.spdm.entity.Cliente;
import br.com.a2dm.spdm.entity.Event;
import br.com.a2dm.spdm.entity.Form;
import br.com.a2dm.spdm.service.ClienteService;
import br.com.a2dm.spdm.service.EventService;
import br.com.a2dm.spdm.service.FormService;

@RequestScoped
@ManagedBean
public class SugestaoPedidoBean extends AbstractBean<Event, EventService>
{
	
	private List<Cliente> listaCliente;
	
	public SugestaoPedidoBean()
	{
		super(EventService.getInstancia());
		this.ACTION_SEARCH = "sugestaoPedido";
		this.pageTitle = "Sugestão de Pedido";
		
		MenuControl.ativarMenu("flgMenuMan");
		MenuControl.ativarSubMenu("flgMenuManCli");
	}
	
	public void visualizar()
	{
		try
		{
			Form form = new Form();
			form.setIdEvent(this.getEntity().getId_event());
			
			List<Form> forms = FormService.getInstancia().pesquisar(form, 0);
			
			if (forms != null && forms.size() > 0) {
				Optional<Form> formImagemOptional = this.getObjectImage(forms);
				forms = this.getForms(forms);				
				setEntity(new Event(formImagemOptional.isPresent() ? formImagemOptional.get().getUrl() : null, forms));
			} else {
				setEntity(new Event());
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
	protected int getJoinPesquisar() {
		return EventService.JOIN_CLIENTE;
	}
	
	public void aprovar() 
	{		
		try
		{
			if(this.getEntity() != null)
			{
				this.validarAprovar();
				EventService.getInstancia().aprovar(this.getEntity());
				FacesMessage message = new FacesMessage("A Sugestão de Pedido foi aprovada com sucesso!");
				message.setSeverity(FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance().addMessage(null, message);
			}
		}
		catch (Exception e) 
		{
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}		
	}

	private void validarAprovar() throws Exception {
		this.getEntity().setForms(null);
		if(this.getEntity().getIdCliente() == null) {
			throw new Exception("O campo Cliente é obrigatório!");			
		}
	}
	
	public void reprovar() 
	{		
		try
		{
			if(this.getEntity() != null)
			{
				EventService.getInstancia().reprovar(this.getEntity());
				FacesMessage message = new FacesMessage("A Sugestão de Pedido foi reprovada com sucesso!");
				message.setSeverity(FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance().addMessage(null, message);
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
	protected void completarPosPesquisar() throws Exception {
		//CARREGANDO LISTA DE CLIENTES		
		Cliente cliente = new Cliente();
		cliente.setFlgAtivo("S");		
		List<Cliente> resultCli = ClienteService.getInstancia().pesquisar(cliente, 0);
		
		Cliente cli = new Cliente();
		cli.setDesCliente("Todos os Clientes");
		
		List<Cliente> listaCliente = new ArrayList<Cliente>();
		listaCliente.add(cli);
		listaCliente.addAll(resultCli);
		
		this.setListaCliente(listaCliente);
	}

	private List<Form> getForms(List<Form> forms) {
		return forms.stream().filter(x -> x.getValue() != null)
				             .collect(Collectors.toList());
	}

	private Optional<Form> getObjectImage(List<Form> forms) {
		return forms.stream().filter(x -> x.getLabel().equalsIgnoreCase(FormService.OBJECT_CAPTURE_IMAGE))
							 .findFirst();
	}

	public List<Cliente> getListaCliente() {
		return listaCliente;
	}

	public void setListaCliente(List<Cliente> listaCliente) {
		this.listaCliente = listaCliente;
	}
}
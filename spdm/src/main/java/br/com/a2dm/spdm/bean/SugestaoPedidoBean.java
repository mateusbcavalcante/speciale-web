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
import br.com.a2dm.spdm.entity.Item;
import br.com.a2dm.spdm.entity.OpcaoEntrega;
import br.com.a2dm.spdm.entity.SugestaoPedido;
import br.com.a2dm.spdm.service.ClienteService;
import br.com.a2dm.spdm.service.ItemService;
import br.com.a2dm.spdm.service.SugestaoPedidoService;

@RequestScoped
@ManagedBean
public class SugestaoPedidoBean extends AbstractBean<SugestaoPedido, SugestaoPedidoService>
{
	
	private List<Cliente> listaCliente;
	
	public SugestaoPedidoBean()
	{
		super(SugestaoPedidoService.getInstancia());
		this.ACTION_SEARCH = "sugestaoPedido";
		this.pageTitle = "Sugestão de Pedido";
		
		MenuControl.ativarMenu("flgMenuMan");
		MenuControl.ativarSubMenu("flgMenuManCli");
	}
	
	public void visualizar()
	{
		try
		{
			Item Item = new Item();
			Item.setIdSugestaoPedido(this.getEntity().getIdSugestaoPedido());
			
			List<Item> Items = ItemService.getInstancia().pesquisar(Item, 0);
			
			if (Items != null && Items.size() > 0) {
				Optional<Item> ItemImagemOptional = this.getObjectImage(Items);
				Items = this.getItems(Items);				
				setEntity(new SugestaoPedido(ItemImagemOptional.isPresent() ? ItemImagemOptional.get().getUrl() : null, Items));
			} else {
				setEntity(new SugestaoPedido());
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
		return SugestaoPedidoService.JOIN_PEDIDO_OPCAO_ENTREGA;
	}
	
	public void aprovar() 
	{		
		try
		{
			if(this.getEntity() != null)
			{
				// this.validarAprovar();
				this.getEntity().setItens(null);
				SugestaoPedidoService.getInstancia().aprovar(this.getEntity());
				this.getEntity().setOpcaoEntrega(this.getEntity().getOpcaoEntrega());
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

	/*
	private void validarAprovar() throws Exception {
		this.getEntity().setItens(null);
		if(this.getEntity().getIdCliente() == null) {
			throw new Exception("O campo Cliente é obrigatório!");			
		}
	}
	*/
	
	public void reprovar() 
	{		
		try
		{
			if(this.getEntity() != null)
			{
				SugestaoPedidoService.getInstancia().reprovar(this.getEntity());
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

	private List<Item> getItems(List<Item> Items) {
		return Items.stream().filter(x -> x.getValue() != null)
				             .collect(Collectors.toList());
	}

	private Optional<Item> getObjectImage(List<Item> Items) {
		return Items.stream().filter(x -> x.getLabel().equalsIgnoreCase(ItemService.OBJECT_CAPTURE_IMAGE))
							 .findFirst();
	}

	public List<Cliente> getListaCliente() {
		return listaCliente;
	}

	public void setListaCliente(List<Cliente> listaCliente) {
		this.listaCliente = listaCliente;
	}
}
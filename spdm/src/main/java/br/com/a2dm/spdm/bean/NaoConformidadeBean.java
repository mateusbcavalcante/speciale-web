package br.com.a2dm.spdm.bean;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
<<<<<<< HEAD
import javax.faces.event.ActionEvent;
=======
>>>>>>> 9135b59 (ajuste nao conformidade)

import br.com.a2dm.brcmn.dto.ProdutoDTO;
import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.brcmn.util.jsf.JSFUtil;
import br.com.a2dm.brcmn.util.jsf.Variaveis;
import br.com.a2dm.spdm.config.MenuControl;
import br.com.a2dm.spdm.entity.Cliente;
import br.com.a2dm.spdm.entity.NaoConformidade;
import br.com.a2dm.spdm.entity.Produto;
import br.com.a2dm.spdm.omie.service.OmieProdutoService;
import br.com.a2dm.spdm.service.ClienteService;
import br.com.a2dm.spdm.service.NaoConformidadeService;
import br.com.a2dm.spdm.service.ProdutoService;

@RequestScoped
@ManagedBean
public class NaoConformidadeBean extends AbstractBean<NaoConformidade, NaoConformidadeService>
{
	private JSFUtil util = new JSFUtil();
	
	private List<Cliente> listaCliente;
	private List<Produto> listaProduto;
	private Produto produtoSelecionado;
	private Produto produto;
	private String informacao;
	
	@ManagedProperty("#{uploadBean}")
	private UploadBean uploadBean;

	public NaoConformidadeBean()
	{
		super(NaoConformidadeService.getInstancia());

		this.ACTION_SEARCH = "naoConformidade";
		this.pageTitle = "Não Conformidade";
		
		MenuControl.ativarMenu("flgMenuMan");
		MenuControl.ativarSubMenu("flgMenuManCli");
	}
	
	@Override
	protected void setValoresDefault() throws Exception
	{		
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
	
	public void buscarProdutos() throws Exception {
		this.setProdutoSelecionado(null);
		this.setInformacao(null);
		this.setProduto(new Produto());
		this.iniciaListaProdutos();
		
		if (this.getEntity().getIdCliente() != null) 
		{
			Cliente cliente = new Cliente();
			cliente.setFlgAtivo("S");
			cliente.setIdCliente(this.getEntity().getIdCliente());
			cliente = ClienteService.getInstancia().get(cliente, 0);
			
			if (cliente != null) {
				
				if (cliente.getIdExternoOmie() == null) {
					this.setInformacao("O cliente selecionado não possui código externo.");
				} else if (cliente.getIdTabelaPrecoOmie() == null) {
					this.setInformacao("O cliente selecionado não possui tabela preço.");
				} else {
					List<ProdutoDTO> listaProduto = OmieProdutoService.getInstance().listarProdutosPorCliente(this.getEntity().getIdCliente());
					
					if (listaProduto != null
							&& listaProduto.size() > 0) {
						for (ProdutoDTO element: listaProduto) { 
							Produto produto = new Produto();
							produto.setIdProduto(element.getIdProduto());
							produto.setDesProduto(element.getDesProduto());
							produto.setValorUnitario(element.getValorUnitario());
							produto.setQtdLoteMinimo(element.getQtdLoteMinimo());
							produto.setQtdMultiplo(element.getQtdMultiplo());
							
							this.getListaProduto().add(produto);
						}
					} else {
						this.setInformacao("O cliente selecionado não tem produto vinculado.");
					}
				}
			
			}
		}
	}
	
	private void iniciaListaProdutos()
	{
		ArrayList<Produto> lista = new ArrayList<Produto>();
		Produto produto = new Produto();
		produto.setIdProduto(null);
		produto.setDesProduto("Escolha o Produto");		
		lista.add(produto);
		
		this.setListaProduto(lista);
	}
	
	public void buscarInformacoes() throws Exception {
		this.setInformacao(null);
		if (this.getProduto().getIdProduto() != null && this.getProduto().getIdProduto().longValue() > 0) {
			Optional<Produto> produtoOptional = this.getListaProduto().stream()
					                                   .filter(x -> x.getIdProduto() == this.getProduto().getIdProduto())
					                                   .findFirst();
			
			if (produtoOptional.isPresent()) {
				produtoSelecionado = produtoOptional.get();
				
				if (produtoSelecionado.getValorUnitario() == null || produtoSelecionado.getValorUnitario() <= 0) {
					this.setInformacao("O produto selecionado não tem valor unitário.");
				}
			}
					                                
		} else {
			setProdutoSelecionado(null);
		}
	}
	
	public BigInteger getIdNaoConformidade() {
		try {
			NaoConformidade nc = NaoConformidadeService.getInstancia().pesquisar(getEntity(), 0).get(0);
			return nc.getIdNaoConformidade();
		}catch (Exception e)
		{
			FacesMessage message = new FacesMessage(e.getMessage());
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			if(e.getMessage() == null)
				FacesContext.getCurrentInstance().addMessage("", message);
			else
				FacesContext.getCurrentInstance().addMessage(null, message);
		}
		return null;
	}
	
	@Override
	protected void validarInserir() throws Exception {
		if(this.getEntity().getLote() == null || this.getEntity().getLote().longValue() <= 0) {
			throw new Exception("O campo Lote é obrigatório.");
		}
		
		if(this.getEntity().getData() == null || "".equals(this.getEntity().getData().toString())) {
			throw new Exception("O campo Data é obrigatório.");
		}
		
		if(this.getEntity().getIdCliente() == null || this.getEntity().getIdCliente().longValue() <= 0) {
			throw new Exception("O campo Cliente é obrigatório.");
		}
		
		if(this.getProduto() == null || this.getProduto().getIdProduto() == null) {
			throw new Exception("O campo Produto é obrigatório.");
		}
		
		if(this.getEntity().getQuantidade() == null) {
			throw new Exception("O campo Quantidade é obrigatório.");
		}
		
		if(this.getEntity().getSetor() == null || "".equals(this.getEntity().getSetor())) {
			throw new Exception("O campo Setor é obrigatório.");
		}
		
		if(this.getEntity().getTipo() == null || "".equals(this.getEntity().getTipo())) {
			throw new Exception("O campo Tipo é obrigatório.");
		}
	}
	
	@Override
	protected int getJoinPesquisar()
	{
		return NaoConformidadeService.JOIN_CLIENTE;
	}
	
	@Override
	protected void completarInserir() throws Exception {
		if(this.produto != null) {
			this.getEntity().setIdProduto(this.produto.getIdProduto());
			this.getEntity().setDescricaoProduto(produto.getDesProduto());
		}
		this.getEntity().setDatCadastro(new Date());
		this.getEntity().setAtivo(true);
		this.getEntity().setIdUsuarioCad(util.getUsuarioLogado().getIdUsuario());
	}
	
	@Override
	protected void completarAlterar() throws Exception {
		this.validarInserir();		
		this.getEntity().setDatAlteracao(new Date());
		this.getEntity().setIdUsuarioAlt(util.getUsuarioLogado().getIdUsuario());
	}
	
	public void inativar() {		
		try {
			if(this.getEntity() != null) {
				if(validarAcesso(Variaveis.ACAO_INATIVAR)) {
					NaoConformidadeService.getInstancia().inativar(this.getEntity());
					
					FacesMessage message = new FacesMessage("Registro inativado com sucesso!");
					message.setSeverity(FacesMessage.SEVERITY_INFO);
					FacesContext.getCurrentInstance().addMessage(null, message);
				}
			}
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}		
	}
	
	public void ativar() {		
		try {
			if(this.getEntity() != null) {
				if(validarAcesso(Variaveis.ACAO_ATIVAR)) {
					NaoConformidadeService.getInstancia().ativar(this.getEntity());
					
					FacesMessage message = new FacesMessage("Registro ativado com sucesso!");
					message.setSeverity(FacesMessage.SEVERITY_INFO);
					FacesContext.getCurrentInstance().addMessage(null, message);
				}
			}
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}		
	}

	public List<Cliente> getListaCliente() {
		return listaCliente;
	}

	public void setListaCliente(List<Cliente> listaCliente) {
		this.listaCliente = listaCliente;
	}

	public Produto getProdutoSelecionado() {
		return produtoSelecionado;
	}

	public void setProdutoSelecionado(Produto produtoSelecionado) {
		this.produtoSelecionado = produtoSelecionado;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public String getInformacao() {
		return informacao;
	}

	public void setInformacao(String informacao) {
		this.informacao = informacao;
	}

	public List<Produto> getListaProduto() {
		return listaProduto;
	}

	public void setListaProduto(List<Produto> listaProduto) {
		this.listaProduto = listaProduto;
	}
	
	public UploadBean getUploadBean() {
		return uploadBean;
	}

	public void setUploadBean(UploadBean uploadBean) {
		this.uploadBean = uploadBean;
	}
	
}
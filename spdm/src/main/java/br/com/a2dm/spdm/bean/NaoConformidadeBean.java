package br.com.a2dm.spdm.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.ActionEvent;

import br.com.a2dm.brcmn.dto.ProdutoDTO;
import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.spdm.config.MenuControl;
import br.com.a2dm.spdm.entity.Cliente;
import br.com.a2dm.spdm.entity.NaoConformidade;
import br.com.a2dm.spdm.entity.Produto;
import br.com.a2dm.spdm.omie.service.OmieProdutoService;
import br.com.a2dm.spdm.service.ClienteService;
import br.com.a2dm.spdm.service.NaoConformidadeService;

@RequestScoped
@ManagedBean
public class NaoConformidadeBean extends AbstractBean<NaoConformidade, NaoConformidadeService>
{
	
	private List<Cliente> listaCliente;
	private List<Produto> listaProduto;
	private Produto produtoSelecionado;
	private Produto produto;
	private String informacao;
	
	
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
	
	@Override
	public void inserir(ActionEvent event) {
		super.inserir(event);
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
	
}
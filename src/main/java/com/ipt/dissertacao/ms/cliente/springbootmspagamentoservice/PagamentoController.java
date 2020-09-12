package com.ipt.dissertacao.ms.cliente.springbootmspagamentoservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import com.ipt.dissertacao.ms.cliente.springbootmspagamentoservice.repositorios.*;
import com.ipt.dissertacao.ms.cliente.springbootmspagamentoservice.business.PagamentoActions;
import com.ipt.dissertacao.ms.cliente.springbootmspagamentoservice.entidades.*;

@RestController
public class PagamentoController {
	@Autowired
	OrdemPagamentoRepository ordem_repository;
	@Autowired
	DICTRepository dict_repository;

	@GetMapping("/pagamentos/cliente/{id_cliente}")
	public List<OrdemPagamento> pagamentosRecuperarPorCliente(@PathVariable long id_cliente) {
		return ordem_repository.findAllByIdCliente(id_cliente);
	}

	@GetMapping("/pagamentos/{id_pagamento}")
	public OrdemPagamento pagamentosRecuperar(@PathVariable long id_pagamento) {
		return ordem_repository.findById(id_pagamento);
	}

	@PostMapping("/pagamentos")
	public ResponseEntity<?> pagamentoRegistrar(@RequestBody OrdemPagamento op) {
		if (op != null) {

			try {
				PagamentoActions.CriarPagamento(op);

				op.setContaDestino(dict_repository.save(op.getContaDestino()));
				op.setContaOrigem(dict_repository.save(op.getContaOrigem()));
				OrdemPagamento myOp = ordem_repository.save(op);

				return ResponseEntity.created(new URI(String.format("pagamentos/%d", op.getId())))
						.body(myOp);
			} catch (Exception e) {
				return new ResponseEntity<String>(String.format("Erro no processamento: %s", e.getMessage()),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return new ResponseEntity<String>(String.format("Favor informar uma ordem de pagamento"),
				HttpStatus.BAD_REQUEST);
	}

	@PutMapping("/pagamentos/{id_pagamento}")
	public ResponseEntity<?> pagamentoAtualizar(@PathVariable long id_pagamento, @RequestBody OrdemPagamento pgto) {
		if (id_pagamento == 0 || pgto == null)
			return new ResponseEntity<String>(String.format("Favor informar uma ordem de pagamento"),
					HttpStatus.BAD_REQUEST);

		try {
			OrdemPagamento op = ordem_repository.findById(id_pagamento);

			if (op == null)
				return new ResponseEntity<String>(String.format("Ordem de pagamento informada não existe"),
						HttpStatus.NOT_FOUND);

			PagamentoActions.AlterarPagamento(op, pgto);

			dict_repository.save(op.getContaDestino());
			dict_repository.save(op.getContaOrigem());
			ordem_repository.save(op);

			return ResponseEntity.accepted().body("Ordem de pagamento atualizada");
		} catch (Exception e) {
			return new ResponseEntity<String>(String.format("Erro no processamento: %s", e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/pagamentos/{id_pagamento}")
	public ResponseEntity<?> pagamentoDeletar(@PathVariable long id_pagamento) {
		if (id_pagamento == 0)
			return new ResponseEntity<String>(String.format("Favor informar uma ordem de pagamento"),
					HttpStatus.BAD_REQUEST);

		try {
			OrdemPagamento pgto = ordem_repository.findById(id_pagamento);

			PagamentoActions.ExcluirPagamento(pgto);

			ordem_repository.save(pgto);

			return ResponseEntity.accepted().body("Ordem de pagamento cancelada com sucesso");

		} catch (Exception e) {
			return new ResponseEntity<String>(String.format("Erro no processamento: %s", e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}

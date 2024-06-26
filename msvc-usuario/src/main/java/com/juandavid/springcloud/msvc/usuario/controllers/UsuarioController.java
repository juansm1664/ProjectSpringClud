package com.juandavid.springcloud.msvc.usuario.controllers;

import com.juandavid.springcloud.msvc.usuario.models.entity.Usuario;
import com.juandavid.springcloud.msvc.usuario.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static java.lang.System.err;

@RestController
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @GetMapping
    public List<Usuario> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable Long id) {
        Optional<Usuario> usuarioOptional = service.porId(id);
        if (usuarioOptional.isPresent()) {
            return  ResponseEntity.ok().body(usuarioOptional.get());
        }
        return  ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Usuario usuario, BindingResult result) {

        if(result.hasErrors()) {
            return validar(result);
        }

        if(!usuario.getEmail().isEmpty() && service.existePorEmail(usuario.getEmail())) {
            return  ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Email ya existe"));
        }


        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(usuario));
    }


    //Editar usuario por id
    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@Valid @RequestBody Usuario usuario, BindingResult result, @PathVariable Long id) {

        if(result.hasErrors()) {
            return validar(result);
        }

        Optional<Usuario> o = service.porId(id);
        if (o.isPresent()){
            Usuario usuarioDb = o.get();
            if(!usuario.getEmail().isEmpty() &&
                    !usuario.getEmail().equalsIgnoreCase(usuarioDb.getEmail()) &&
                    service.buscarPorEmail(usuario.getEmail()).isPresent()) {
                return  ResponseEntity.badRequest()
                        .body(Collections.singletonMap("error", "Email ya existe"));
            }
            usuarioDb.setNombre(usuario.getNombre());
            usuarioDb.setEmail(usuario.getEmail());
            usuarioDb.setEmail(usuario.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(usuarioDb));
        }
        return  ResponseEntity.notFound().build();

    }
    //Eliminar por id

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Optional<Usuario> o = service.porId(id);
        if (o.isPresent()) {
            service.eliminar(id);
            return ResponseEntity.noContent().build();

        }
        return  ResponseEntity.notFound().build();
    }
    //Consulta para mostrar los usuarios por ids - keys: ids params: 1,3,9(ids de usuarios)
    @GetMapping("/usuarios-por-curso")
    public ResponseEntity<?> obtenerUsuarioCurso(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(service.listarPorIds(ids));
    }


    //Metodos static

    private static ResponseEntity<Map<String, String>> validar(BindingResult result) {
        Map<String,String> errores = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errores.put(err.getField(), "El campo " + err.getField() + " " +  err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errores);
    }

}

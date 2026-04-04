package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<Restaurante> restaurantes = new ArrayList<>();

    protected Admin() {}

    public Admin(String email, String nome, String password) {
        super(email, nome, password, "ADMIN");
    }

    public List<Restaurante> getRestaurantes() {
        return restaurantes;
    }

    public void setRestaurantes(List<Restaurante> restaurantes) {
        this.restaurantes = restaurantes;
    }

    public void addRestaurante(Restaurante restaurante) {
        this.restaurantes.add(restaurante);
        restaurante.setAdmin(this);
    }
}
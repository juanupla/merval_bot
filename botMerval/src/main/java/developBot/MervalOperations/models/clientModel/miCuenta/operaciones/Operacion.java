package developBot.MervalOperations.models.clientModel.miCuenta.operaciones;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
//operaciones devuelve una lista de este objeto
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operacion {
    private Integer numero;
    private LocalDateTime fechaOrden;
    private String tipo;
    private String estado;
    private String mercado;
    private String simbolo;
    private Long cantidad;
    private Double monto;
    private String modalidad;
    private Double precio;
    private LocalDateTime fechaOperada;
    private Double cantidadOperada;
    private Double precioOperado;
    private Double montoOperado;
    private String plazo;
}

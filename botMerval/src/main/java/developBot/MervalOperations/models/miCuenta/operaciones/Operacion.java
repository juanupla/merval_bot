package developBot.MervalOperations.models.miCuenta.operaciones;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
//operaciones devuelve una lista de este objeto
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operacion {
    private Long numero;
    private LocalDateTime fechaOrden;
    private String tipo;
    private String estado;
    private String mercado;
    private String simbolo;
    private int cantidad;
    private Long monto;
    private String modalidad;
    private Long precio;
    private LocalDateTime fechaOperada;
    private Long cantidadOperada;
    private Long precioOperado;
    private Long montoOperado;
    private String plazo;
}

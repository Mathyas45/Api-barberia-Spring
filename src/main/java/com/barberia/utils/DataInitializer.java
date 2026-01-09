//package com.barberia.utils;
//
//import com.barberia.models.Negocio;
//import com.barberia.models.Permiso;
//import com.barberia.models.Rol;
//import com.barberia.models.Usuario;
//import com.barberia.repositories.NegocioRepository;
//import com.barberia.repositories.PermisoRepository;
//import com.barberia.repositories.RolRepository;
//import com.barberia.repositories.UsuarioRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.util.Set;
//
///**
// * Inicializador de datos para testing
// *
// * CommandLineRunner:
// * Se ejecuta automáticamente al iniciar la aplicación
// *
// * CREA:
// * - Permisos básicos
// * - Roles básicos con sus permisos
// * - Usuarios de prueba con diferentes roles
// *
// * IMPORTANTE:
// * En producción, usa migraciones de BD (Flyway/Liquibase)
// * Este enfoque es solo para desarrollo y pruebas
// */
//@Component
//@Slf4j
//public class DataInitializer implements CommandLineRunner {
//
//    private final PermisoRepository permisoRepository;
//    private final RolRepository rolRepository;
//    private final UsuarioRepository usuarioRepository;
//    private final NegocioRepository negocioRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public DataInitializer(PermisoRepository permisoRepository, RolRepository rolRepository,
//                          UsuarioRepository usuarioRepository, NegocioRepository negocioRepository,
//                          PasswordEncoder passwordEncoder) {
//        this.permisoRepository = permisoRepository;
//        this.rolRepository = rolRepository;
//        this.usuarioRepository = usuarioRepository;
//        this.negocioRepository = negocioRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//
//        // Solo inicializa si no hay datos
//        if (permisoRepository.count() > 0 || rolRepository.count() > 0 || usuarioRepository.count() > 0) {
//            log.info("✅ Datos ya inicializados, omitiendo...");
//            return;
//        }
//
//        log.info("Inicializando datos de prueba...");
//
//        // 0. CREAR NEGOCIOS
//        Negocio negocio1 = createNegocio("Barbería El Estilo", "12345678901", "Av. Principal 123");
//        Negocio negocio2 = createNegocio("Barbería Corte Moderno", "98765432109", "Calle Los Barberos 456");
//
//        // 1. CREAR PERMISOS
//        Permiso readClients = createPermiso("READ_CLIENTS", "Ver clientes");
//        Permiso createClients = createPermiso("CREATE_CLIENTS", "Crear clientes");
//        Permiso updateClients = createPermiso("UPDATE_CLIENTS", "Actualizar clientes");
//        Permiso deleteClients = createPermiso("DELETE_CLIENTS", "Eliminar clientes");
//
//        Permiso readBooking = createPermiso("READ_BOOKING", "Ver reservas");
//        Permiso createBooking = createPermiso("CREATE_BOOKING", "Crear reservas");
//        Permiso manageBooking = createPermiso("MANAGE_BOOKING", "Gestionar reservas");
//
//        Permiso deleteUsers = createPermiso("DELETE_USERS", "Eliminar usuarios");
//        Permiso specialAccess = createPermiso("SPECIAL_ACCESS", "Acceso especial");
//
//        // 2. CREAR ROLES CON PERMISOS
//
//        // Rol ADMIN: Tiene todos los permisos
//        Rol adminRole = createRole(
//                "ADMIN",
//                "Administrador del sistema",
//                Set.of(readClients, createClients, updateClients, deleteClients,
//                       readBooking, createBooking, manageBooking,
//                       deleteUsers, specialAccess)
//        );
//
//        // Rol MANAGER: Permisos de gestión pero no eliminar usuarios
//        Rol managerRole = createRole(
//                "MANAGER",
//                "Gerente de la barbería",
//                Set.of(readClients, createClients, updateClients,
//                       readBooking, createBooking, manageBooking)
//        );
//
//        // Rol USER: Permisos básicos
//        Rol userRole = createRole(
//                "USER",
//                "Usuario estándar",
//                Set.of(readClients, readBooking, createBooking)
//        );
//
//        // 3. CREAR USUARIOS DE PRUEBA
//
//        // Usuario ADMIN para negocio 1
//        createUser(
//                "Admin",
//                "admin@barberia.com",
//                "admin123",
//                Set.of(adminRole),
//                negocio1,
//                1
//        );
//
//        // Usuario MANAGER para negocio 1
//        createUser(
//                "Manager",
//                "manager@barberia.com",
//                "manager123",
//                Set.of(managerRole),
//                negocio1
//        );
//
//        // Usuario normal para negocio 2
//        createUser(
//                "Juan Pérez",
//                "user@barberia.com",
//                "user123",
//                Set.of(userRole),
//                negocio2
//        );
//
//        // Usuario con múltiples roles para negocio 2
//        createUser(
//                "Super User",
//                "super@barberia.com",
//                "super123",
//                Set.of(adminRole, managerRole),
//                negocio2
//        );
//
//        log.info("✅ Datos de prueba inicializados correctamente");
//        log.info("📝 Usuarios de prueba:");
//        log.info("   - admin@barberia.com / admin123 (ADMIN)");
//        log.info("   - manager@barberia.com / manager123 (MANAGER)");
//        log.info("   - user@barberia.com / user123 (USER)");
//        log.info("   - super@barberia.com / super123 (ADMIN + MANAGER)");
//    }
//
//    private Permiso createPermiso(String name, String description) {
//        Permiso permiso = Permiso.builder()
//                .name(name)
//                .description(description)
//                .build();
//        return permisoRepository.save(permiso);
//    }
//
//    private Rol createRole(String name, String description, Set<Permiso> permissions) {
//        Rol rol = Rol.builder()
//                .name(name)
//                .description(description)
//                .permissions(permissions)
//                .build();
//        return rolRepository.save(rol);
//    }
//
//    private Negocio createNegocio(String nombre, String ruc, String direccion) {
//        Negocio negocio = Negocio.builder()
//                .nombre(nombre)
//                .ruc(ruc)
//                .direccion(direccion)
//                .telefono("999999999")
//                .email("contacto@" + nombre.toLowerCase().replace(" ", "") + ".com")
//                .estado("ACTIVO")
//                .build();
//        return negocioRepository.save(negocio);
//    }
//
//    private void createUser(String name, String email, String password, Set<Rol> roles, Negocio negocio , Long usuarioRegistroId) {
//        Usuario usuario = Usuario.builder()
//                .name(name)
//                .email(email)
//                .password(passwordEncoder.encode(password))
//                .regEstado(1)
//                .roles(roles)
//                .negocioId(negocio.getId())
//                .negocio(negocio)
//                .usuarioRegistroId(1) // Asignar un valor predeterminado
//                .build();
//        usuarioRepository.save(usuario);
//    }
//}

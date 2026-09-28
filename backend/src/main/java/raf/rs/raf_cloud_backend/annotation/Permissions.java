package raf.rs.raf_cloud_backend.annotation;

public class Permissions {
    public static final String CREATE_USER = "can_create_users";
    public static final String READ_USER = "can_read_users";
    public static final String UPDATE_USER = "can_update_users";
    public static final String DELETE_USER = "can_delete_users";

    public static final String SEARCH_MACHINES = "can_search_machines";
    public static final String POWER_ON_MACHINE = "can_start_machines";
    public static final String POWER_OFF_MACHINE = "can_stop_machines";
    public static final String RESTART_MACHINE = "can_restart_machines";
    public static final String CREATE_MACHINE = "can_create_machines";
    public static final String DESTROY_MACHINE = "can_destroy_machines";
    public static final String READ_MACHINES = "can_read_machines";
}
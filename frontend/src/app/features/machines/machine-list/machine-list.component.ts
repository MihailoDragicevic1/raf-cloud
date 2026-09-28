import { Component, OnInit, OnDestroy } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import { Machine, MachineService } from '../../../core/services/machine.service';
import { AuthService } from '../../../core/services/auth.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-machine-list',
  templateUrl: './machine-list.component.html',
  styleUrls: ['./machine-list.component.scss']
})
export class MachineListComponent implements OnInit, OnDestroy {

  machines: Machine[] = [];
  displayedColumns: string[] = ['id', 'name', 'type', 'location', 'status', 'actions'];
  filterForm!: FormGroup;

  currentUser: any;
  isAdmin: boolean = false;
  permissions: string[] = [];

  loadingMachineIds: number[] = [];

  private machineStatusChangedSubscription?: Subscription;
  private machineChangedSubscription?: Subscription;
  wsConnectionStatus: boolean = false;
  scheduledOperations: any[] = [];
  selectedMachineId: number | null = null;
  showScheduleModal: boolean = false;
  scheduleForm!: FormGroup;


  constructor(
    private machineService: MachineService,
    private fb: FormBuilder,
    private authService: AuthService,
    private webSocketService: WebSocketService
  ) {}

  ngOnInit(): void {
    this.filterForm = this.fb.group({
      name: [''],
      type: [''],
      location: [''],
      status: ['']
    });

    this.updatePermissions();

    this.currentUser = this.authService.getCurrentUser();
    this.isAdmin = this.authService.hasRole('Administrator');
    this.permissions = this.authService.getUserPermissions();

    if (this.can('can_read_machines')) {
      this.loadMachines();
    } else {
      console.warn('Korisnik nema can_read_machines permisiju');
    }

    this.subscribeToWebSocketEvents();

    this.scheduleForm = this.fb.group({
      operation: ['', Validators.required],
      scheduledDateTime: ['', Validators.required]
    });

    this.webSocketService.getConnectionStatus().subscribe(status => {
      this.wsConnectionStatus = status;
      console.log('WebSocket status:', status ? 'Povezan' : 'Nije povezan');
    });

    const token = localStorage.getItem('token');
    if (token) {
      const parts = token.split('.');
      if (parts.length === 3) {
        try {
          const payload = JSON.parse(atob(parts[1]));
          console.log('Email:', payload.email || payload.sub);
          console.log('Permissions u tokenu:', payload.permissions);
        } catch (e) {
          console.error('Greška pri dekodiranju tokena:', e);
        }
      }
    }
    setInterval(() => {
      if (this.can('can_read_machines')) {
        this.loadMachines();
      }
    }, 10000);
  }


  ngOnDestroy(): void {
    if (this.machineStatusChangedSubscription) {
      this.machineStatusChangedSubscription.unsubscribe();
    }
    if (this.machineChangedSubscription) {
      this.machineChangedSubscription.unsubscribe();
    }
    console.log('Machine-list komponenta uništena - WebSocket subscription-i izbrisani');
  }


  private subscribeToWebSocketEvents(): void {
    this.machineStatusChangedSubscription = this.webSocketService.machineStatusChanged.subscribe(
      (data: any) => {
        if (data && data.id) {
          console.log('WebSocket: Mašina je promenila status -', data);
          this.updateMachineInList(data);
        }
      }
    );

    this.machineChangedSubscription = this.webSocketService.machineChanged.subscribe(
      (data: any) => {
        if (data) {
          console.log('WebSocket: Mašina promenjena -', data);
          this.handleMachineChanged(data);
        }
      }
    );
  }


  private updateMachineInList(updatedMachine: any): void {
    const index = this.machines.findIndex(m => m.id === updatedMachine.id);
    if (index !== -1) {
      this.machines[index].status = updatedMachine.status;
      console.log('Mašina ažurirana u listi:', updatedMachine.id);
    }
  }


  private handleMachineChanged(machine: any): void {
    const index = this.machines.findIndex(m => m.id === machine.id);

    if (index !== -1) {
      this.machines[index] = { ...this.machines[index], ...machine };
      console.log('Mašina ažurirana:', machine.id);
    } else {
      if (this.isAdmin || machine.ownerUsername === this.currentUser.username) {
        this.machines.push(machine);
        console.log('Nova mašina dodata u listu:', machine.id);
      }
    }
  }

  loadMachines(): void {
    this.machineService.getMachinesObservable().subscribe(
      (machines) => {
        this.machines = machines;
        console.log(' Mašine učitane u komponentu:', machines);
      },
      (error) => {
        console.error(' Greška pri učitavanju mašina:', error);
      }
    );
  }

  applyFilter(): void {
    const filterValues = this.filterForm.value;
    let filtered = this.machineService.filterMachines(filterValues);

    if (!this.isAdmin) {
      filtered = filtered.filter(m => m.ownerUsername === this.currentUser.username);
    }

    this.machines = filtered;
  }

  clearFilter(): void {
    this.filterForm.reset();
    this.loadMachines();
  }

  can(permission: string): boolean {
    return this.isAdmin || this.permissions.includes(permission);
  }

  disableDuringOperation(id: number): boolean {
    return this.loadingMachineIds.includes(id);
  }

  deleteMachine(id: number): void {
    if (!this.can('can_destroy_machines')) return;
    this.machineService.deleteMachine(id);

    setTimeout(() => {
      this.loadMachines();
    }, 10000);
  }

  turnOn(id: number): void {
    if (!this.can('can_start_machines')) return;
    this.loadingMachineIds.push(id);
    this.machineService.turnOn(id);
    setTimeout(() => {
      this.loadingMachineIds = this.loadingMachineIds.filter(x => x !== id);
      this.loadMachines();
    }, 10000);
  }

  turnOff(id: number): void {
    if (!this.can('can_stop_machines')) return;
    this.loadingMachineIds.push(id);
    this.machineService.turnOff(id);
    setTimeout(() => {
      this.loadingMachineIds = this.loadingMachineIds.filter(x => x !== id);
      this.loadMachines();
    }, 10000);
  }

  restart(id: number): void {
    if (!this.can('can_restart_machines')) return;
    this.loadingMachineIds.push(id);
    this.machineService.restart(id);
    setTimeout(() => {
      this.loadingMachineIds = this.loadingMachineIds.filter(x => x !== id);
      this.loadMachines();
    }, 11000);
  }

  openScheduleModal(machineId: number): void {
    console.log('openScheduleModal pozvan za machineId:', machineId);
    console.log('Permissions:', this.permissions);

    this.selectedMachineId = machineId;
    this.showScheduleModal = true;
    this.scheduleForm.reset();
  }

  closeScheduleModal(): void {
    this.showScheduleModal = false;
    this.selectedMachineId = null;
    this.scheduleForm.reset();
  }

  submitScheduleOperation(): void {
    if (!this.scheduleForm.valid || !this.selectedMachineId) {
      alert('Molim popunite sve polje');
      return;
    }

    const operation = this.scheduleForm.value.operation;
    const scheduledTime = this.scheduleForm.value.scheduledDateTime;

    this.machineService.scheduleOperation(this.selectedMachineId, operation, scheduledTime);
    this.loadScheduledOperations(this.selectedMachineId);
    this.closeScheduleModal();
  }

  loadScheduledOperations(machineId: number): void {
    this.scheduledOperations = this.machineService.getScheduledOperationsByMachine(machineId);
  }

  cancelScheduledOperation(operationId: any): void {
    if (confirm('Da li ste sigurni da želite da otkazatete ovu operaciju?')) {
      this.machineService.cancelScheduledOperation(operationId);
      if (this.selectedMachineId) {
        this.loadScheduledOperations(this.selectedMachineId);
      }
    }
  }

  getStatusColor(status: string): string {
    switch(status) {
      case 'PENDING': return 'warn';
      case 'RUNNING': return 'accent';
      case 'COMPLETED': return 'primary';
      case 'FAILED': return 'warn';
      default: return '';
    }
  }

  private updatePermissions(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.isAdmin = this.authService.hasRole('Administrator');
    this.permissions = this.authService.getUserPermissions();

  }
}

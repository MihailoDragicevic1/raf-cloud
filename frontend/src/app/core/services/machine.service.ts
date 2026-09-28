import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface Machine {
  id: number;
  name: string;
  type: string;
  location: string;
  status: string;
  createdAt?: string;
  ownerUsername: string;
  isBusy?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class MachineService {

  private apiUrl = 'http://localhost:8081/machines';
  private machines: Machine[] = [];
  scheduledOperations: any[] = [];

  constructor(private authService: AuthService, private http: HttpClient) {
    this.loadScheduledOperationsFromLocalStorage();
    this.loadMachinesFromBackend();
  }

  private loadMachinesFromBackend(): void {
    const token = this.authService.getToken();
    if (!token) return;

    const headers = { 'Authorization': `Bearer ${token}` };

    this.http.get<Machine[]>(this.apiUrl, { headers }).subscribe(
      (machines) => {
        this.machines = machines;
        console.log('Mašine učitane sa backend-a:', machines);
      },
      (error) => {
        console.error(' Greška pri učitavanju mašina:', error);
      }
    );
  }

  getAllMachines(): Machine[] {
    return this.machines;
  }

  refreshMachines(): void {
    this.loadMachinesFromBackend();
  }

  filterMachines(filter: any): Machine[] {
    return this.machines.filter(machine => {
      return (
        (!filter.name || machine.name.toLowerCase().includes(filter.name.toLowerCase())) &&
        (!filter.type || machine.type.toLowerCase().includes(filter.type.toLowerCase())) &&
        (!filter.location || machine.location.toLowerCase().includes(filter.location.toLowerCase())) &&
        (!filter.status || machine.status.toLowerCase() === filter.status.toLowerCase())
      );
    });
  }

  addMachine(name: string): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    const token = this.authService.getToken();
    const headers = { 'Authorization': `Bearer ${token}` };

    const newMachine = {
      name: name,
      type: 'CNC',
      description: 'Nova mašina'
    };

    this.http.post<Machine>(this.apiUrl, newMachine, { headers }).subscribe(
      (machine) => {
        console.log(' Mašina kreirana:', machine);
        this.machines.push(machine);
        alert(`Mašina "${machine.name}" je uspešno dodata!`);
        this.refreshMachines();
      },
      (error) => {
        console.error(' Greška pri kreiranju mašine:', error);
        alert('Greška pri kreiranju mašine: ' + (error.error?.message || error.message));
      }
    );
  }

  deleteMachine(id: number): void {
    if (!this.authService.hasPermission('can_destroy_machines') &&
      !this.authService.hasRole('Administrator')) {
      alert('Nemate dozvolu za brisanje mašina.');
      return;
    }

    const token = this.authService.getToken();
    const headers = { 'Authorization': `Bearer ${token}` };

    this.http.delete(`${this.apiUrl}/${id}`, { headers }).subscribe(
      () => {
        console.log(' Mašina obrisana:', id);
        this.machines = this.machines.filter(m => m.id !== id);
        alert('Mašina je uspešno obrisana!');
      },
      (error) => {
        console.error(' Greška pri brisanju mašine:', error);
        alert('Greška pri brisanju mašine: ' + (error.error?.message || error.message));
      }
    );
  }

  turnOn(id: number): void {
    const machine = this.machines.find(m => m.id === id);
    if (!machine) {
      alert('Mašina nije pronađena!');
      return;
    }

    if (!this.authService.hasPermission('can_start_machines')) {
      alert('Nemate dozvolu da uključite mašinu.');
      return;
    }

    const token = this.authService.getToken();
    const headers = { 'Authorization': `Bearer ${token}` };

    machine.isBusy = true;
    alert(`Paljenje mašine "${machine.name}"...`);

    this.http.post(`${this.apiUrl}/${id}/start`, {}, { headers }).subscribe(
      (response: any) => {
        console.log(' Mašina se pali:', response);
        setTimeout(() => {
          machine.isBusy = false;
          this.refreshMachines();
          alert(`Mašina "${machine.name}" je uspešno uključena!`);
        }, 10000);
      },
      (error) => {
        console.error(' Greška pri paljenju:', error);
        machine.isBusy = false;
        alert('Greška: ' + (error.error?.message || error.message));
      }
    );
  }

  turnOff(id: number): void {
    const machine = this.machines.find(m => m.id === id);
    if (!machine) {
      alert('Mašina nije pronađena!');
      return;
    }

    if (!this.authService.hasPermission('can_stop_machines')) {
      alert('Nemate dozvolu da ugasite mašinu.');
      return;
    }

    const token = this.authService.getToken();
    const headers = { 'Authorization': `Bearer ${token}` };

    machine.isBusy = true;
    alert(`Gašenje mašine "${machine.name}"...`);

    this.http.post(`${this.apiUrl}/${id}/stop`, {}, { headers }).subscribe(
      (response: any) => {
        console.log(' Mašina se gasi:', response);
        setTimeout(() => {
          machine.isBusy = false;
          this.refreshMachines();
          alert(`Mašina "${machine.name}" je uspešno ugašena!`);
        }, 10000);
      },
      (error) => {
        console.error('Greška pri gašenju:', error);
        machine.isBusy = false;
        alert('Greška: ' + (error.error?.message || error.message));
      }
    );
  }

  restart(id: number): void {
    const machine = this.machines.find(m => m.id === id);
    if (!machine) {
      alert('Mašina nije pronađena!');
      return;
    }

    if (!this.authService.hasPermission('can_restart_machines')) {
      alert('Nemate dozvolu da restartujete mašinu.');
      return;
    }

    const token = this.authService.getToken();
    const headers = { 'Authorization': `Bearer ${token}` };

    machine.isBusy = true;
    alert(`Restartovanje mašine "${machine.name}"...`);

    this.http.post(`${this.apiUrl}/${id}/restart`, {}, { headers }).subscribe(
      (response: any) => {
        console.log(' Mašina se restartuje:', response);
        setTimeout(() => {
          machine.isBusy = false;
          this.refreshMachines();
          alert(`Mašina "${machine.name}" je uspešno restartovana!`);
        }, 11000);
      },
      (error) => {
        console.error(' Greška pri restartovanju:', error);
        machine.isBusy = false;
        alert('Greška: ' + (error.error?.message || error.message));
      }
    );
  }

  scheduleOperation(machineId: number, operation: string, scheduledTime: string): void {
    const user = this.authService.getCurrentUser();
    if (!user) {
      alert('Niste prijavljeni!');
      return;
    }

    const token = this.authService.getToken();

    const request = {
      machineId: machineId,
      operation: operation.toUpperCase(),
      scheduledTime: scheduledTime
    };

    const headers = { 'Authorization': `Bearer ${token}` };

    this.http.post(
      `${this.apiUrl}/${machineId}/scheduled-operations`,
      request,
      { headers: headers }
    ).subscribe(
      (response: any) => {
        console.log(' Operacija zakazana na backendu:', response);

        const scheduledOp = {
          id: response.id || Math.random(),
          machineId: machineId,
          operation: operation,
          scheduledTime: scheduledTime,
          status: 'PENDING',
          createdAt: new Date().toISOString(),
          executedAt: null,
          errorMessage: null
        };

        this.scheduledOperations.push(scheduledOp);
        this.saveScheduledOperationsToLocalStorage();
        alert(`Operacija "${operation}" je zakazana za ${scheduledTime}`);
      },
      (error: any) => {
        console.error(' Greška pri zakazivanju:', error);

        if (error.status === 403) {
          alert('Nemate dozvolu za zakazivanje operacija!');
        } else if (error.status === 401) {
          alert('Sesija je istekla! Prijavite se ponovo.');
        } else {
          alert('Greška: ' + (error.error?.message || error.message));
        }
      }
    );
  }

  getScheduledOperationsByMachine(machineId: number): any[] {
    return this.scheduledOperations.filter(op => op.machineId === machineId);
  }

  cancelScheduledOperation(operationId: any): void {
    if (!this.authService.hasPermission('can_destroy_machines')) {
      alert('Nemate dozvolu za otkazivanje operacija.');
      return;
    }

    const index = this.scheduledOperations.findIndex(op => op.id === operationId);
    if (index !== -1) {
      const op = this.scheduledOperations[index];

      if (op.status !== 'PENDING') {
        alert(`Može se otkazati samo PENDING operacija (trenutni status: ${op.status})`);
        return;
      }

      this.scheduledOperations.splice(index, 1);
      this.saveScheduledOperationsToLocalStorage();
      console.log('Zakazana operacija otkazana:', operationId);
      alert('Operacija je otkazana');
    }
  }

  private saveScheduledOperationsToLocalStorage(): void {
    localStorage.setItem('scheduledOperations', JSON.stringify(this.scheduledOperations));
  }

  private loadScheduledOperationsFromLocalStorage(): void {
    const stored = localStorage.getItem('scheduledOperations');
    if (stored) {
      this.scheduledOperations = JSON.parse(stored);
    }
  }
  getMachinesObservable(): Observable<Machine[]> {
    const token = this.authService.getToken();
    const headers = { 'Authorization': `Bearer ${token}` };

    return this.http.get<Machine[]>(this.apiUrl, { headers }).pipe(
      tap(machines => {
        this.machines = machines;
        console.log(' Mašine učitane kroz Observable:', machines);
      })
    );
  }
}

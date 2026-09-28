import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ErrorService {
  private errors = [
    {
      id: 1,
      machineName: 'Server 1',
      operation: 'Start',
      message: 'Greška pri pokretanju mašine.',
      timestamp: '2025-11-07 10:24:00',
      user: 'admin'
    },
    {
      id: 2,
      machineName: 'Server 2',
      operation: 'Stop',
      message: 'Greška prilikom gašenja mašine.',
      timestamp: '2025-11-06 15:40:00',
      user: 'marko'
    }
  ];

  getErrors() {
    return this.errors;
  }
}

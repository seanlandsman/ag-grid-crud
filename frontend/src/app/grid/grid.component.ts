import {Component, OnInit} from '@angular/core';
import {CellValueChangedEvent, ColDef, ColumnApi, GridApi} from 'ag-grid';
import {AthleteService} from '../services/athlete.service';
import {Athlete} from '../model/athlete.model';
import {StaticDataService} from '../services/static-data.service';
import {Country} from '../model/country.model';

// we need to import this as we're making use of enterprise features, such as the richSelect cell editor
import 'ag-grid-enterprise';

@Component({
    selector: 'app-grid',
    templateUrl: './grid.component.html',
    styleUrls: ['./grid.component.css']
})
export class GridComponent implements OnInit {
    // row data and column definitions
    private rowData: Athlete[];
    private columnDefs: ColDef[];

    // gridApi and columnApi
    private api: GridApi;
    private columnApi: ColumnApi;

    private showEditScreen: boolean = false;

    // inject the athleteService
    constructor(private athleteService: AthleteService,
                staticDataService: StaticDataService) {

        staticDataService.countries().subscribe(
            countries => this.columnDefs = this.createColumnDefs(countries),
            error => console.log(error)
        );
    }

    // on init, read to the athlete data
    ngOnInit() {
        this.setAthleteRowData();
    }

    setAthleteRowData() {
        this.athleteService.findAll().subscribe(
            athletes => this.rowData = athletes,
            error => console.log(error)
        )
    }

    onAthleteSaved(savedAthlete: Athlete) {
        const updates = this.api.updateRowData(
            {
                add: [savedAthlete]
            }
        );

        this.showEditScreen = false;
    }

    // one grid initialisation, grap the APIs and auto resize the columns to fit the available space
    onGridReady(params): void {
        this.api = params.api;
        this.columnApi = params.columnApi;

        this.api.sizeColumnsToFit();
    }

    // create some simple column definitions
    private createColumnDefs(countries: Country[]) {
        return [
            {
                field: 'name',
                editable: true,
                checkboxSelection: true
            },
            {
                field: 'country',
                cellRenderer: (params) => params.data.country.name,
                editable: true,
                cellEditor: 'richSelect',
                cellEditorParams: {
                    values: countries,
                    cellRenderer: (params) => params.value.name
                }
            },
            {
                field: 'results',
                valueGetter: (params) => params.data.results.length
            }
        ]
    }

    rowsSelected() {
        return this.api && this.api.getSelectedRows().length > 0;
    }

    onCellValueChanged(params: CellValueChangedEvent) {
        // todo compare new & old value to prevent unnecessary saves

        // todo - on success show user a message, on error revert grid value & display message
        // (params.data => updated row, params.newValue, params.oldValue => { id: x, name: y } // for country for eg

        this.athleteService.save(params.data)
            .subscribe(
                savedAthlete => console.log('Athlete Saved'),
                error => console.log(error)
            )
    }

    insertNewRow() {
        this.showEditScreen = true;
    }

    deleteSelectedRows() {
        const selectRows = this.api.getSelectedRows();

        // todo - on success show user a message

        // first pass - only one row at a time
        if (selectRows.length === 1) {
            const rowToDelete = selectRows[0];
            this.athleteService.delete(rowToDelete)
                .subscribe(
                    success => {
                        console.log('Deleted athlete');
                        this.setAthleteRowData();
                    },
                    error => console.log(error)
                )
        }
    }
}
